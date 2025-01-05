package com.example.analyzer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
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

public class ContactList extends AppCompatActivity {
    private static final int REQUEST_CREATE_LIST = 100;
    private static final int REQUEST_MODIFY_LIST = 101;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private List<Map<String, Object>> contactLists;
    private ContactListAdapter adapter;
    private ListView contactListsView;
    private TextView noListsText;
    private ProgressBar loadingIndicator;
    private Button createListButton;
    private Button otherUserListsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list);

        initializeUI();
        loadContactLists();
    }

    private void initializeUI() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        contactListsView = findViewById(R.id.contactListsView);
        createListButton = findViewById(R.id.createListButton);
        otherUserListsButton = findViewById(R.id.otherUserListsButton);
        noListsText = findViewById(R.id.noListsText);
        loadingIndicator = findViewById(R.id.loadingIndicator);

        contactLists = new ArrayList<>();
        adapter = new ContactListAdapter(this, contactLists);
        contactListsView.setAdapter(adapter);

        createListButton.setOnClickListener(v -> navigateToCreateList());
        otherUserListsButton.setOnClickListener(v -> navigateToOtherUserLists());

        // Handle single press to navigate to modify page
        contactListsView.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
            String listId = (String) contactLists.get(position).get("id");
            navigateToModifyList(listId);
        });

        // Handle long press for delete or toggle visibility
        contactListsView.setOnItemLongClickListener((AdapterView<?> parent, View view, int position, long id) -> {
            showPopupMenu(position, view);
            return true;
        });
    }

    private void loadContactLists() {
        showLoading(true);
        String userId = auth.getCurrentUser().getUid();

        db.collection("contactLists")
                .whereEqualTo("ownerId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    contactLists.clear();

                    queryDocumentSnapshots.forEach(document -> {
                        Map<String, Object> list = new HashMap<>();
                        try {
                            list.put("id", document.getId());
                            list.put("name", document.getString("name") != null ? document.getString("name") : "Unnamed List");
                            list.put("isPublic", document.getBoolean("isPublic") != null && document.getBoolean("isPublic"));
                            list.put("contacts", document.get("contacts") instanceof List ? document.get("contacts") : new ArrayList<>());
                        } catch (Exception e) {
                            Log.e("ContactListActivity", "Error parsing document: " + document.getId(), e);
                        }

                        Log.d("ContactListActivity", "Loaded List: " + list);
                        contactLists.add(list);
                    });

                    updateUI();
                    showLoading(false);
                })
                .addOnFailureListener(e -> {
                    Log.e("ContactListActivity", "Error loading lists", e);
                    Toast.makeText(this, "Failed to load lists", Toast.LENGTH_SHORT).show();
                    showLoading(false);
                });
    }

    private void updateUI() {
        if (contactLists.isEmpty()) {
            noListsText.setVisibility(View.VISIBLE);
            contactListsView.setVisibility(View.GONE);
        } else {
            noListsText.setVisibility(View.GONE);
            contactListsView.setVisibility(View.VISIBLE);
            adapter.notifyDataSetChanged();
        }
    }

    private void navigateToCreateList() {
        Intent intent = new Intent(this, CreateContactList.class);
        startActivityForResult(intent, REQUEST_CREATE_LIST);
    }

    private void navigateToModifyList(String listId) {
        Intent intent = new Intent(this, ModifyList.class);
        intent.putExtra("LIST_ID", listId);
        startActivityForResult(intent, REQUEST_MODIFY_LIST);
    }

    private void navigateToOtherUserLists() {
        Intent intent = new Intent(this, OtherUserLists.class);
        startActivity(intent);
    }

    private void showPopupMenu(int position, View view) {
        Map<String, Object> selectedList = contactLists.get(position);
        boolean isPublic = selectedList.containsKey("isPublic") && (boolean) selectedList.get("isPublic");
        String listId = (String) selectedList.get("id");

        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenu().add(isPublic ? "Make Private" : "Make Public");
        popupMenu.getMenu().add("Delete");

        popupMenu.setOnMenuItemClickListener(item -> {
            String action = item.getTitle().toString();
            switch (action) {
                case "Make Private":
                case "Make Public":
                    toggleListVisibility(listId, !isPublic);
                    return true;
                case "Delete":
                    deleteList(listId, position);
                    return true;
                default:
                    return false;
            }
        });

        popupMenu.show();
    }

    private void deleteList(String listId, int position) {
        showLoading(true);
        db.collection("contactLists").document(listId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "List deleted successfully", Toast.LENGTH_SHORT).show();
                    contactLists.remove(position);
                    adapter.notifyDataSetChanged();
                    showLoading(false);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to delete list", Toast.LENGTH_SHORT).show();
                    Log.e("ContactListActivity", "Error deleting list", e);
                    showLoading(false);
                });
    }

    private void toggleListVisibility(String listId, boolean newStatus) {
        showLoading(true);
        db.collection("contactLists").document(listId)
                .update("isPublic", newStatus)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "List visibility updated", Toast.LENGTH_SHORT).show();
                    loadContactLists();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update list visibility", Toast.LENGTH_SHORT).show();
                    Log.e("ContactListActivity", "Error updating visibility", e);
                    showLoading(false);
                });
    }

    private void showLoading(boolean show) {
        loadingIndicator.setVisibility(show ? View.VISIBLE : View.GONE);
        contactListsView.setVisibility(show ? View.GONE : View.VISIBLE);
        createListButton.setEnabled(!show);
        otherUserListsButton.setEnabled(!show);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CREATE_LIST || requestCode == REQUEST_MODIFY_LIST) {
            if (resultCode == RESULT_OK) {
                loadContactLists(); // Reload the contact lists
            }
        }
    }
}
