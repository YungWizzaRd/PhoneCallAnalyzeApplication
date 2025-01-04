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

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.contact_list_item, parent, false);
        }

        TextView listNameTextView = convertView.findViewById(R.id.listNameTextView);
        TextView listStatusTextView = convertView.findViewById(R.id.listStatusTextView);
        TextView listCountTextView = convertView.findViewById(R.id.listCountTextView);

        Map<String, Object> list = contactLists.get(position);
        String listName = (String) list.get("name");
        boolean isPublic = (boolean) list.get("isPublic");
        int contactCount = (int) list.get("contactCount");

        listNameTextView.setText(listName);
        listStatusTextView.setText(isPublic ? "Public" : "Private");
        listCountTextView.setText(contactCount + " Contacts");

        return convertView;
    }
}
