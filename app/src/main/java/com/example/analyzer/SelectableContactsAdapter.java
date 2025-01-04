package com.example.analyzer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SelectableContactsAdapter extends android.widget.BaseAdapter {
    private Context context;
    private List<Map<String, String>> contacts;
    private Set<Map<String, String>> selectedContacts;

    public SelectableContactsAdapter(Context context, List<Map<String, String>> contacts) {
        this.context = context;
        this.contacts = contacts;
        this.selectedContacts = new HashSet<>();
    }

    public Set<Map<String, String>> getSelectedContacts() {
        return selectedContacts;
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

        Map<String, String> contact = contacts.get(position);

        CheckBox checkBox = convertView.findViewById(R.id.selectContactCheckBox);
        TextView nameTextView = convertView.findViewById(R.id.contactNameTextView);
        TextView phoneTextView = convertView.findViewById(R.id.contactPhoneTextView);

        nameTextView.setText(contact.get("name"));
        phoneTextView.setText(contact.get("phone"));

        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedContacts.add(contact);
            } else {
                selectedContacts.remove(contact);
            }
        });

        return convertView;
    }
}
