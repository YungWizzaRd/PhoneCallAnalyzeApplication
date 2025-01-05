package com.example.analyzer;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OtherUserLists extends AppCompatActivity {
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
        loadOtherUserLists();
    }

    private void initializeUI() {
        db = FirebaseFirestore.getInstance();

        otherUserListsView = findViewById(R.id.otherUserListsView);
        noListsText = findViewById(R.id.noListsText);
        loadingIndicator = findViewById(R.id.loadingIndicator);

        otherUserLists = new ArrayList<>();
        adapter = new ContactListAdapter(this, otherUserLists);
        otherUserListsView.setAdapter(adapter);
    }

    private void loadOtherUserLists() {
        showLoading(true);

        db.collection("contactLists")
                .whereEqualTo("isPublic", true) // Fetch only public/shared lists
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    otherUserLists.clear();
                    queryDocumentSnapshots.forEach(document -> {
                        Map<String, Object> list = document.getData();
                        list.put("id", document.getId());
                        otherUserLists.add(list);
                    });

                    updateUI();
                    showLoading(false);
                })
                .addOnFailureListener(e -> {
                    Log.e("OtherUserListsActivity", "Error loading lists", e);
                    Toast.makeText(this, "Failed to load lists", Toast.LENGTH_SHORT).show();
                    showLoading(false);
                });
    }

    private void updateUI() {
        if (otherUserLists.isEmpty()) {
            noListsText.setText("No public lists available.");
            noListsText.setVisibility(View.VISIBLE);
            otherUserListsView.setVisibility(View.GONE);
        } else {
            noListsText.setVisibility(View.GONE);
            otherUserListsView.setVisibility(View.VISIBLE);
            adapter.notifyDataSetChanged();
        }
    }

    private void showLoading(boolean show) {
        loadingIndicator.setVisibility(show ? View.VISIBLE : View.GONE);
        otherUserListsView.setVisibility(show ? View.GONE : View.VISIBLE);
    }
}
