package com.example.analyzer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ContactListAdapter extends BaseAdapter {
    private final Context context;
    private final List<Map<String, Object>> contactLists;

    public ContactListAdapter(Context context, List<Map<String, Object>> contactLists) {
        this.context = context;
        this.contactLists = contactLists;
    }

    @Override
    public int getCount() {
        return contactLists.size();
    }

    @Override
    public Object getItem(int position) {
        return contactLists.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.contact_list_item, parent, false);
        }

        Map<String, Object> list = contactLists.get(position);

        TextView nameTextView = convertView.findViewById(R.id.listNameTextView);
        TextView statusTextView = convertView.findViewById(R.id.listStatusTextView);
        TextView contactCountTextView = convertView.findViewById(R.id.listContactCountTextView);

        try {
            // Parse list name
            String name = list.get("name") != null ? list.get("name").toString() : "Unnamed List";

            // Parse isPublic status
            boolean isPublic = list.containsKey("isPublic") && list.get("isPublic") instanceof Boolean && (boolean) list.get("isPublic");

            // Parse contacts
            List<Map<String, String>> contacts = list.get("contacts") instanceof List ? (List<Map<String, String>>) list.get("contacts") : new ArrayList<>();

            // Bind data to views
            nameTextView.setText(name);
            statusTextView.setText(isPublic ? "Public" : "Private");
            contactCountTextView.setText("Contacts: " + contacts.size());
        } catch (Exception e) {
            Log.e("ContactListAdapter", "Error in getView at position: " + position, e);
            statusTextView.setText("Private"); // Default fallback
            contactCountTextView.setText("Contacts: 0");
        }

        return convertView;
    }


}
