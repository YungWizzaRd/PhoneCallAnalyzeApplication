package com.example.analyzer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;
import java.util.Map;

public class ModifyListAdapter extends BaseAdapter {
    private final Context context;
    private final List<Map<String, String>> contacts;

    public ModifyListAdapter(Context context, List<Map<String, String>> contacts) {
        this.context = context;
        this.contacts = contacts;
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
            convertView = LayoutInflater.from(context).inflate(R.layout.modify_list_contact_item, parent, false);
        }

        Map<String, String> contact = contacts.get(position);

        TextView nameTextView = convertView.findViewById(R.id.contactNameTextView);
        TextView phoneTextView = convertView.findViewById(R.id.contactPhoneTextView);

        nameTextView.setText(contact.get("name"));
        phoneTextView.setText(contact.get("phone"));

        return convertView;
    }
}
