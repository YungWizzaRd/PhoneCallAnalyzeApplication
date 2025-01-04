package com.example.analyzer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private Button stat, settings, exit, login, createContactList;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Firebase Authentication
        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        // UI element initialization
        stat = findViewById(R.id.stat);
        settings = findViewById(R.id.settings);
        exit = findViewById(R.id.exit);
        login = findViewById(R.id.loginButton);
        createContactList = findViewById(R.id.createContactList);

        // Update UI based on login state
        if (currentUser != null) {
            login.setText("Logout");
            createContactList.setEnabled(true);
        } else {
            login.setText("Login");
            createContactList.setEnabled(false);
        }

        // Button listeners
        stat.setOnClickListener(v -> startStat());
        settings.setOnClickListener(v -> startSettings());
        login.setOnClickListener(v -> {
            if (currentUser != null) {
                auth.signOut();
                Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
                recreate(); // Refresh the activity
            } else {
                startActivity(new Intent(this, Login.class));
            }
        });
        createContactList.setOnClickListener(v -> {
            if (currentUser != null) {
                Intent intent = new Intent(this, ContactList.class);
                startActivity(intent);
            }
        });
        exit.setOnClickListener(v -> finishAffinity());
    }

    private void startStat() {
        startActivity(new Intent(this, Stat.class));
    }

    private void startSettings() {
        startActivity(new Intent(this, Settings.class));
    }
}
