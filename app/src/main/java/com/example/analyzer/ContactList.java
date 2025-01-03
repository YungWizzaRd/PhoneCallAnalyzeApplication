package com.example.analyzer;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ContactList extends AppCompatActivity {
    private FirebaseFirestore db;
    private EditText listNameField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list);

        db = FirebaseFirestore.getInstance();

        listNameField = findViewById(R.id.listNameField);
        Button saveButton = findViewById(R.id.saveListButton);

        saveButton.setOnClickListener(v -> {
            String listName = listNameField.getText().toString();
            if (!listName.isEmpty()) {
                Map<String, Object> contactList = new HashMap<>();
                contactList.put("name", listName);
                contactList.put("ownerId", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());

                db.collection("contactLists").add(contactList)
                        .addOnSuccessListener(documentReference -> {
                            Toast.makeText(this, "List created", Toast.LENGTH_SHORT).show();
                            finish();
                        })
                        .addOnFailureListener(e -> Toast.makeText(this, "Failed to create list", Toast.LENGTH_SHORT).show());
            } else {
                Toast.makeText(this, "List name cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
