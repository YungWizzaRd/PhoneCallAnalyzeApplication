package com.example.analyzer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContactList extends AppCompatActivity {
    private static final int REQUEST_READ_CONTACTS = 1;
    private FirebaseFirestore db;
    private SelectableContactsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list);

        db = FirebaseFirestore.getInstance();
        ListView contactsListView = findViewById(R.id.contactsListView);
        Button saveButton = findViewById(R.id.saveButton);

        // Request permissions and load contacts
        if (checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, REQUEST_READ_CONTACTS);
        } else {
            loadContacts(contactsListView);
        }

        // Save selected contacts to Firebase
        saveButton.setOnClickListener(v -> {
            List<Map<String, String>> selectedContacts = new ArrayList<>(adapter.getSelectedContacts());
            if (!selectedContacts.isEmpty()) {
                saveContactListToFirebase(selectedContacts);
            } else {
                Toast.makeText(this, "No contacts selected", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadContacts(ListView contactsListView) {
        List<Map<String, String>> contacts = getPhoneContacts();
        adapter = new SelectableContactsAdapter(this, contacts);
        contactsListView.setAdapter(adapter);
    }

    private List<Map<String, String>> getPhoneContacts() {
        List<Map<String, String>> contacts = new ArrayList<>();
        Cursor cursor = getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                null,
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {
                @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                @SuppressLint("Range") String phone = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                Map<String, String> contact = new HashMap<>();
                contact.put("name", name);
                contact.put("phone", phone);
                contacts.add(contact);
            }
            cursor.close();
        }
        return contacts;
    }

    private void saveContactListToFirebase(List<Map<String, String>> selectedContacts) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Map<String, Object> contactList = new HashMap<>();
        contactList.put("ownerId", userId);
        contactList.put("contacts", selectedContacts);

        db.collection("contactLists")
                .add(contactList)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "List saved successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to save list", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                ListView contactsListView = findViewById(R.id.contactsListView);
                loadContacts(contactsListView);
            } else {
                Toast.makeText(this, "Permission denied to read contacts", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
