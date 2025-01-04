package com.example.analyzer;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SelectableContactsAdapter extends android.widget.BaseAdapter {
    private final Context context;
    private final List<Map<String, String>> contacts;
    private final Set<Map<String, String>> selectedContacts;
    private boolean isSelectionEnabled;

    public SelectableContactsAdapter(Context context, List<Map<String, String>> contacts) {
        this.context = context;
        this.contacts = new ArrayList<>(contacts); // Initialize with a copy to avoid external modifications
        this.selectedContacts = new HashSet<>();
        this.isSelectionEnabled = true; // Default to selection mode
    }

    public void setSelectionEnabled(boolean enabled) {
        this.isSelectionEnabled = enabled;
        notifyDataSetChanged();
    }

    public Set<Map<String, String>> getSelectedContacts() {
        return selectedContacts;
    }

    public void updateContacts(List<Map<String, String>> updatedContacts) {
        this.contacts.clear();
        this.contacts.addAll(new ArrayList<>(updatedContacts)); // Use a copy for stability
        notifyDataSetChanged(); // Notify the ListView to refresh
    }

    @Override
    public int getCount() {
        return contacts.size();
    }

    @Override
    public Object getItem(int position) {
        return contacts.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.contact_item_with_checkbox, parent, false);
        }

        // Get the current contact
        Map<String, String> contact = contacts.get(position);

        // Find views in the layout
        CheckBox checkBox = convertView.findViewById(R.id.selectContactCheckBox);
        TextView nameTextView = convertView.findViewById(R.id.contactNameTextView);
        TextView phoneTextView = convertView.findViewById(R.id.contactPhoneTextView);

        // Bind contact data to the views
        nameTextView.setText(contact.get("name"));
        phoneTextView.setText(contact.get("phone"));

        // Set up checkbox visibility and functionality
        if (isSelectionEnabled) {
            checkBox.setVisibility(View.VISIBLE);
            checkBox.setOnCheckedChangeListener(null); // Avoid listener trigger during setup
            checkBox.setChecked(selectedContacts.contains(contact));
            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedContacts.add(contact);
                } else {
                    selectedContacts.remove(contact);
                }
                Log.d("SelectableContactsAdapter", "Selected Contacts: " + selectedContacts.size());
            });
        } else {
            checkBox.setVisibility(View.GONE);
        }

        return convertView;
    }
}
