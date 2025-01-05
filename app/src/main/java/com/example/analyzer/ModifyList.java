package com.example.analyzer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
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
    private CheckBox publicListCheckbox;
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
        publicListCheckbox = findViewById(R.id.publicListCheckbox);
        contactsListView = findViewById(R.id.contactsListView);
        addContactsButton = findViewById(R.id.addContactsButton);
        saveButton = findViewById(R.id.saveButton);

        contacts = new ArrayList<>();
        adapter = new ModifyListAdapter(this, contacts);
        contactsListView.setAdapter(adapter);

        addContactsButton.setOnClickListener(v -> navigateToAddContacts());
        saveButton.setOnClickListener(v -> saveListChanges());

        contactsListView.setOnItemClickListener((parent, view, position, id) -> {
            showContactEditDialog(position);
        });
    }

    private void loadListDetails() {
        db.collection("contactLists").document(listId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Log.d("ModifyListActivity", "Document Data: " + documentSnapshot.getData());

                        String listName = documentSnapshot.getString("name");
                        boolean isPublic = documentSnapshot.getBoolean("isPublic") != null && documentSnapshot.getBoolean("isPublic");

                        listNameEditText.setText(listName);
                        publicListCheckbox.setChecked(isPublic);

                        List<Map<String, String>> contactList = new ArrayList<>();
                        if (documentSnapshot.get("contacts") instanceof List) {
                            contactList = (List<Map<String, String>>) documentSnapshot.get("contacts");
                        }

                        contacts.clear();
                        contacts.addAll(contactList);
                        adapter.notifyDataSetChanged();
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
        boolean isPublic = publicListCheckbox.isChecked();

        if (updatedName.isEmpty()) {
            Toast.makeText(this, "List name cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if (contacts.isEmpty()) {
            deleteList();
            return;
        }

        Log.d("ModifyListActivity", "Saving changes: Name=" + updatedName + ", isPublic=" + isPublic);

        Map<String, Object> updatedData = new HashMap<>();
        updatedData.put("name", updatedName);
        updatedData.put("isPublic", isPublic);
        updatedData.put("contacts", contacts);

        db.collection("contactLists").document(listId)
                .update(updatedData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "List updated successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save changes", Toast.LENGTH_SHORT).show();
                });
    }

    private void deleteList() {
        db.collection("contactLists").document(listId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "List deleted as it contains no contacts", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to delete the list", Toast.LENGTH_SHORT).show();
                });
    }

    private void showContactEditDialog(int position) {
        Map<String, String> contact = contacts.get(position);

        Log.d("ModifyListActivity", "Opening edit dialog for: " + contact);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Contact");

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_contact, null);
        builder.setView(dialogView);

        EditText nameEditText = dialogView.findViewById(R.id.editContactName);
        EditText phoneEditText = dialogView.findViewById(R.id.editContactPhone);
        Button saveButton = dialogView.findViewById(R.id.saveContactButton);
        Button deleteButton = dialogView.findViewById(R.id.deleteContactButton);
        Button cancelButton = dialogView.findViewById(R.id.cancelContactButton);

        nameEditText.setText(contact.get("name"));
        phoneEditText.setText(contact.get("phone"));

        AlertDialog dialog = builder.create();

        saveButton.setOnClickListener(v -> {
            String updatedName = nameEditText.getText().toString().trim();
            String updatedPhone = phoneEditText.getText().toString().trim();

            if (updatedName.isEmpty() || updatedPhone.isEmpty()) {
                Toast.makeText(this, "Both name and phone must be filled", Toast.LENGTH_SHORT).show();
                return;
            }

            contact.put("name", updatedName);
            contact.put("phone", updatedPhone);
            adapter.notifyDataSetChanged();
            Toast.makeText(this, "Contact updated", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        deleteButton.setOnClickListener(v -> {
            contacts.remove(position);
            adapter.notifyDataSetChanged();
            Toast.makeText(this, "Contact deleted", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        cancelButton.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void navigateToAddContacts() {
        // Implement adding contacts functionality if needed
    }
}
