package com.example.analyzer;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;
import android.provider.ContactsContract;

import java.util.ArrayList;
import java.util.List;

public class ContactStatistics {
    private final String phoneNumber;
    private final String contactName;
    private int callCount;
    private long totalDuration;

    public ContactStatistics(Context context, String phoneNumber) {
        this.phoneNumber = phoneNumber;
        this.contactName = getContactNameFromNumber(context);
        this.callCount = 0;
        this.totalDuration = 0;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getContactName() {
        return contactName;
    }

    public int getCallCount() {
        return callCount;
    }

    public long getTotalDuration() {
        return totalDuration;
    }
// Call count calculation
    public void addCall(int callDuration) {
        callCount++;
        totalDuration += callDuration;
    }

    private String getContactNameFromNumber(Context context) {
        ContentResolver contentResolver = context.getContentResolver(); // Access ContentResolver using the provided context
        String contactName = null;

        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cursor = contentResolver.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);

        if (cursor != null) {
            int nameIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME);

            if (nameIndex >= 0 && cursor.moveToFirst()) {
                contactName = cursor.getString(nameIndex);
            }

            cursor.close();
        }

        return contactName != null ? contactName : "Unknown";
    }

    // Method to calculate contact statistics
    public static List<ContactStatistics> calculateContactStatisticsList(Context context) {
        List<ContactStatistics> contactStatisticsList = new ArrayList<>();

        Cursor cursor = context.getContentResolver().query(
                CallLog.Calls.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        if (cursor != null && cursor.moveToFirst()) {
            int numberIndex = cursor.getColumnIndex(CallLog.Calls.NUMBER);
            int durationIndex = cursor.getColumnIndex(CallLog.Calls.DURATION);

            do {
                String phoneNumber = cursor.getString(numberIndex);
                int callDuration = cursor.getInt(durationIndex);

                ContactStatistics contactStats = findContactStatistics(contactStatisticsList, phoneNumber);

                if (contactStats == null) {
                    contactStats = new ContactStatistics(context, phoneNumber);
                    contactStatisticsList.add(contactStats);
                }

                contactStats.addCall(callDuration);

            } while (cursor.moveToNext());

            cursor.close();
        }

        return contactStatisticsList;
    }

    // Method to find a ContactStatistics object in a list by phone number
    private static ContactStatistics findContactStatistics(List<ContactStatistics> list, String phoneNumber) {
        for (ContactStatistics stats : list) {
            if (stats.getPhoneNumber().equals(phoneNumber)) {
                return stats;
            }
        }
        return null;
    }
}

