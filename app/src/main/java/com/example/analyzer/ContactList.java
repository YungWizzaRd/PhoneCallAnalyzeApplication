package com.example.analyzer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ContactList extends AppCompatActivity {
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private List<Map<String, Object>> contactLists;
    private ContactListAdapter adapter;
    private ListView contactListsView;
    private TextView noListsText;
    private ProgressBar loadingIndicator;
    private View createListButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list);

        initializeUI();
        loadContactLists();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload contact lists whenever the activity is resumed
        loadContactLists();
    }

    private void initializeUI() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        contactListsView = findViewById(R.id.contactListsView);
        createListButton = findViewById(R.id.createListButton);
        noListsText = findViewById(R.id.noListsText);
        loadingIndicator = findViewById(R.id.loadingIndicator);

        contactLists = new ArrayList<>();
        adapter = new ContactListAdapter(this, contactLists);
        contactListsView.setAdapter(adapter);

        // Handle "Create List" button click
        createListButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateContactList.class);
            startActivity(intent);
        });

        // Single press: Navigate to Modify page
        contactListsView.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
            String listId = (String) contactLists.get(position).get("id");
            navigateToModifyList(listId);
        });

        // Long press: Show popup menu for status change and delete
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
                        Map<String, Object> list = document.getData();
                        list.put("id", document.getId());
                        list.put("contactCount", ((List<?>) list.get("contacts")).size());
                        contactLists.add(list);
                    });

                    updateUI();
                    showLoading(false);
                })
                .addOnFailureListener(e -> {
                    Log.e("ContactListActivity", "Error loading contact lists", e);
                    Toast.makeText(this, "Failed to load contact lists", Toast.LENGTH_SHORT).show();
                    showLoading(false);
                });
    }

    private void updateUI() {
        if (contactLists.isEmpty()) {
            noListsText.setText("You didn't create a list for now");
            noListsText.setVisibility(View.VISIBLE);
            contactListsView.setVisibility(View.GONE);
        } else {
            noListsText.setVisibility(View.GONE);
            contactListsView.setVisibility(View.VISIBLE);
            adapter.notifyDataSetChanged();
        }
    }

    private void navigateToModifyList(String listId) {
        Intent intent = new Intent(this, ModifyList.class);
        intent.putExtra("LIST_ID", listId);
        startActivity(intent);
    }

    private void showPopupMenu(int position, View view) {
        Map<String, Object> selectedList = contactLists.get(position);
        boolean isPublic = (boolean) selectedList.get("isPublic");
        String listId = (String) selectedList.get("id");

        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenu().add(isPublic ? "Make Private" : "Make Public");
        popupMenu.getMenu().add("Delete");

        popupMenu.setOnMenuItemClickListener(item -> {
            String action = item.getTitle().toString();
            switch (action) {
                case "Make Private":
                case "Make Public":
                    toggleListStatus(listId, !isPublic);
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

    private void toggleListStatus(String listId, boolean newStatus) {
        showLoading(true);
        db.collection("contactLists").document(listId)
                .update("isPublic", newStatus)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "List status updated successfully", Toast.LENGTH_SHORT).show();
                    loadContactLists();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update list status", Toast.LENGTH_SHORT).show();
                    Log.e("ContactListActivity", "Error updating list status", e);
                    showLoading(false);
                });
    }

    private void showLoading(boolean show) {
        loadingIndicator.setVisibility(show ? View.VISIBLE : View.GONE);
        contactListsView.setVisibility(show ? View.GONE : View.VISIBLE);
        createListButton.setEnabled(!show); // Disable button when loading
    }
}
