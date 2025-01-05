package com.example.analyzer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AddContacts extends AppCompatActivity {
    private static final int REQUEST_READ_CONTACTS = 1;

    private SelectableContactsAdapter adapter;
    private EditText searchField;
    private TextView selectedCountTextView; // Selection indicator
    private List<Map<String, String>> allContacts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contacts);

        initializeUI();
        checkPermissions();
    }

    private void initializeUI() {
        searchField = findViewById(R.id.searchField);
        selectedCountTextView = findViewById(R.id.selectedCountTextView); // Selection indicator
        ListView contactsListView = findViewById(R.id.contactsListView);
        Button addButton = findViewById(R.id.addContactsButton);

        allContacts = new ArrayList<>();
        adapter = new SelectableContactsAdapter(this, allContacts);
        contactsListView.setAdapter(adapter);

        addButton.setOnClickListener(v -> addSelectedContacts());

        searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No action required
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterContacts(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // No action required
            }
        });

        updateSelectedCount(); // Initialize the selection count
    }

    private void checkPermissions() {
        if (checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, REQUEST_READ_CONTACTS);
        } else {
            loadContacts();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadContacts();
            } else {
                Toast.makeText(this, "Permission to read contacts is required", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadContacts() {
        allContacts.clear();
        Set<String> uniquePhoneNumbers = new HashSet<>();
        Cursor cursor = getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                null,
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String phone = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

                if (phone != null) {
                    phone = phone.replaceAll("\\s+", "").replaceAll("-", ""); // Normalize phone numbers
                }

                if (phone != null && !uniquePhoneNumbers.contains(phone)) {
                    Map<String, String> contact = new HashMap<>();
                    contact.put("name", name != null ? name : "Unknown");
                    contact.put("phone", phone);
                    allContacts.add(contact);
                    uniquePhoneNumbers.add(phone);
                }
            }
            cursor.close();
        }

        adapter.updateContacts(new ArrayList<>(allContacts));
    }

    private void filterContacts(String query) {
        if (query.trim().isEmpty()) {
            adapter.updateContacts(new ArrayList<>(allContacts));
            return;
        }

        List<Map<String, String>> filteredContacts = new ArrayList<>();
        for (Map<String, String> contact : allContacts) {
            String name = contact.get("name").toLowerCase();
            String phone = contact.get("phone").toLowerCase();

            if (name.contains(query.toLowerCase()) || phone.contains(query.toLowerCase())) {
                filteredContacts.add(contact);
            }
        }

        adapter.updateContacts(filteredContacts);
    }

    private void addSelectedContacts() {
        List<Map<String, String>> selectedContacts = new ArrayList<>(adapter.getSelectedContacts());

        if (selectedContacts.isEmpty()) {
            Toast.makeText(this, "Please select at least one contact.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Pass the selected contacts back to the calling activity
        Intent resultIntent = new Intent();
        resultIntent.putExtra("SELECTED_CONTACTS", (ArrayList<Map<String, String>>) selectedContacts);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private void updateSelectedCount() {
        int count = adapter.getSelectedContacts().size();
        selectedCountTextView.setText("Selected: " + count);
    }
}
