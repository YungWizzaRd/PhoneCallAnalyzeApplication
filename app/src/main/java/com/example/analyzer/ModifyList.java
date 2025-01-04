package com.example.analyzer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModifyList extends AppCompatActivity {
    private FirebaseFirestore db;
    private String listId;

    private EditText listNameEditText;
    private ListView contactsListView;
    private Button addContactsButton;
    private Button saveButton;

    private List<Map<String, String>> contacts;
    private ModifyListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_list);

        db = FirebaseFirestore.getInstance();

        // Get the list ID from the intent
        listId = getIntent().getStringExtra("LIST_ID");
        if (listId == null || listId.isEmpty()) {
            Toast.makeText(this, "Invalid List ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeUI();
        loadListDetails();
    }

    private void initializeUI() {
        listNameEditText = findViewById(R.id.listNameEditText);
        contactsListView = findViewById(R.id.contactsListView);
        addContactsButton = findViewById(R.id.addContactsButton);
        saveButton = findViewById(R.id.saveButton);

        contacts = new ArrayList<>();
        adapter = new ModifyListAdapter(this, contacts); // Use ModifyListAdapter
        contactsListView.setAdapter(adapter);

        // Add contacts button
        addContactsButton.setOnClickListener(v -> navigateToAddContacts());

        // Save button
        saveButton.setOnClickListener(v -> saveListChanges());
    }

    private void loadListDetails() {
        db.collection("contactLists").document(listId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Log.d("ModifyListActivity", "Document Data: " + documentSnapshot.getData());

                        // Set the list name
                        String listName = documentSnapshot.getString("name");
                        listNameEditText.setText(listName);

                        // Parse and log contacts
                        List<Map<String, String>> contactList = new ArrayList<>();
                        if (documentSnapshot.get("contacts") instanceof List) {
                            contactList = (List<Map<String, String>>) documentSnapshot.get("contacts");
                        }

                        // Update contacts and adapter
                        if (contactList != null) {
                            contacts.clear();
                            contacts.addAll(contactList);
                            adapter.notifyDataSetChanged();
                        }
                    } else {
                        Toast.makeText(this, "List not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ModifyListActivity", "Error loading list details", e);
                    Toast.makeText(this, "Failed to load list details", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void saveListChanges() {
        String updatedName = listNameEditText.getText().toString().trim();
        if (updatedName.isEmpty()) {
            Toast.makeText(this, "List name cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        // Prepare updated data
        Map<String, Object> updatedData = new HashMap<>();
        updatedData.put("name", updatedName);
        updatedData.put("contacts", contacts);

        // Save updates to Firestore
        db.collection("contactLists").document(listId)
                .update(updatedData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "List updated successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e("ModifyListActivity", "Error saving list changes", e);
                    Toast.makeText(this, "Failed to save changes", Toast.LENGTH_SHORT).show();
                });
    }

    private void navigateToAddContacts() {
        Intent intent = new Intent(this, AddContactsActivity.class);
        intent.putExtra("LIST_ID", listId);
        startActivityForResult(intent, 100); // Use a request code for results
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            // Reload the list after adding new contacts
            loadListDetails();
        }
    }
}
