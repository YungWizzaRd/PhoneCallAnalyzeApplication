package com.example.analyzer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OtherUserLists extends AppCompatActivity {
    private static final int REQUEST_PERMISSION_CALL_LOG = 101;

    private FirebaseFirestore db;
    private List<Map<String, Object>> otherUserLists;
    private ContactListAdapter adapter;
    private ListView otherUserListsView;
    private TextView noListsText;
    private ProgressBar loadingIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_user_lists);

        initializeUI();
        checkPermissions();
    }

    private void initializeUI() {
        db = FirebaseFirestore.getInstance();

        otherUserListsView = findViewById(R.id.otherUserListsView);
        noListsText = findViewById(R.id.noListsText);
        loadingIndicator = findViewById(R.id.loadingIndicator);

        otherUserLists = new ArrayList<>();
        adapter = new ContactListAdapter(this, otherUserLists);
        otherUserListsView.setAdapter(adapter);

        otherUserListsView.setOnItemClickListener((parent, view, position, id) -> {
            showPopupMenu(position, view);
        });

        loadOtherUserLists();
    }

    private void checkPermissions() {
        if (checkSelfPermission(Manifest.permission.WRITE_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_CALL_LOG}, REQUEST_PERMISSION_CALL_LOG);
        }
    }

    private void loadOtherUserLists() {
        showLoading(true);
        String currentUserId = FirebaseAuth.getInstance().getUid();

        db.collection("contactLists")
                .whereEqualTo("isPublic", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    otherUserLists.clear();

                    queryDocumentSnapshots.forEach(document -> {
                        String ownerId = document.getString("ownerId");
                        if (!ownerId.equals(currentUserId)) {
                            Map<String, Object> list = new HashMap<>();
                            try {
                                list.put("id", document.getId());
                                list.put("name", document.getString("name") != null ? document.getString("name") : "Unnamed List");
                                list.put("contacts", document.get("contacts") instanceof List ? document.get("contacts") : new ArrayList<>());

                                Log.d("OtherUserListsActivity", "Loaded Public List: " + list);
                                otherUserLists.add(list);
                            } catch (Exception e) {
                                Log.e("OtherUserListsActivity", "Error parsing document: " + document.getId(), e);
                            }
                        }
                    });

                    updateUI();
                    showLoading(false);
                })
                .addOnFailureListener(e -> {
                    Log.e("OtherUserListsActivity", "Error loading public lists", e);
                    Toast.makeText(this, "Failed to load public lists", Toast.LENGTH_SHORT).show();
                    showLoading(false);
                });
    }

    private void updateUI() {
        if (otherUserLists.isEmpty()) {
            noListsText.setVisibility(View.VISIBLE);
            otherUserListsView.setVisibility(View.GONE);
        } else {
            noListsText.setVisibility(View.GONE);
            otherUserListsView.setVisibility(View.VISIBLE);
            adapter.notifyDataSetChanged();
        }
    }

    private void showPopupMenu(int position, View view) {
        Map<String, Object> selectedList = otherUserLists.get(position);

        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenu().add("Add to My Lists");
        popupMenu.getMenu().add("Add to Blacklist");
        popupMenu.getMenu().add("Save to Contacts");

        popupMenu.setOnMenuItemClickListener(item -> {
            String action = item.getTitle().toString();
            switch (action) {
                case "Add to My Lists":
                    addToMyLists(selectedList);
                    return true;
                case "Add to Blacklist":
                    addToBlacklist(selectedList);
                    return true;
                case "Save to Contacts":
                    saveToContacts(selectedList);
                    return true;
                default:
                    return false;
            }
        });

        popupMenu.show();
    }

    private void addToMyLists(Map<String, Object> list) {
        String userId = FirebaseAuth.getInstance().getUid();
        Map<String, Object> newList = new HashMap<>(list);

        newList.put("ownerId", userId);
        newList.remove("id");

        db.collection("contactLists")
                .add(newList)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "List added to My Lists", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to add list to My Lists", Toast.LENGTH_SHORT).show();
                });
    }

    private void addToBlacklist(Map<String, Object> list) {
        List<Map<String, String>> contacts = list.get("contacts") instanceof List ? (List<Map<String, String>>) list.get("contacts") : new ArrayList<>();

        for (Map<String, String> contact : contacts) {
            String phone = contact.get("phone");

            // Add the number to the device's blocklist
            try {
                ContentValues values = new ContentValues();
                values.put("number", phone);

                Uri uri = getContentResolver().insert(Uri.parse("content://call_blocking/blocklist"), values);
                if (uri != null) {
                    Toast.makeText(this, "Number added to blocklist: " + phone, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Failed to add number to blocklist: " + phone, Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Log.e("OtherUserListsActivity", "Error adding to blocklist: " + phone, e);
                Toast.makeText(this, "Failed to block number: " + phone, Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void saveToContacts(Map<String, Object> list) {
        List<Map<String, String>> contacts = list.get("contacts") instanceof List ? (List<Map<String, String>>) list.get("contacts") : new ArrayList<>();

        for (Map<String, String> contact : contacts) {
            String name = contact.get("name");
            String phone = contact.get("phone");

            Intent intent = new Intent(ContactsContract.Intents.Insert.ACTION);
            intent.setType(ContactsContract.RawContacts.CONTENT_TYPE);
            intent.putExtra(ContactsContract.Intents.Insert.NAME, name);
            intent.putExtra(ContactsContract.Intents.Insert.PHONE, phone);

            try {
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "Failed to open contacts app", Toast.LENGTH_SHORT).show();
                Log.e("OtherUserListsActivity", "Error saving to contacts", e);
            }
        }
    }

    private void showLoading(boolean show) {
        loadingIndicator.setVisibility(show ? View.VISIBLE : View.GONE);
        otherUserListsView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CALL_LOG) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted to manage call log", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission denied to manage call log", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
