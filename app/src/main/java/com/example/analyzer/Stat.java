package com.example.analyzer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class Stat extends AppCompatActivity {

    private int lastCheckedId=R.id.none;
    private TextView textView;
    private Button filterButton;
    private ImageButton backButton;
    private List<ContactStatistics> contactStatisticsList;

    //function for back button
    public void startMain(View v){
        Intent intent= new Intent(this, MainActivity.class );
        startActivity(intent);
    }
    // function for statistics output
    private void displayStatistics(List<ContactStatistics> displayList) {
        StringBuilder stringBuilder = new StringBuilder();

        for (ContactStatistics stats : displayList) {
            String contactName = stats.getContactName();
            String phoneNumber = stats.getPhoneNumber();
            long totalDuration = stats.getTotalDuration();

            stringBuilder.append(getString(R.string.name)).append(" ").append(contactName).append("\n");
            stringBuilder.append(getString(R.string.number)).append(" ").append(phoneNumber).append("\n");
            stringBuilder.append(getString(R.string.count)).append(" ").append(stats.getCallCount()).append("\n");
            String formattedDuration = formatTotalDuration(totalDuration);
            stringBuilder.append(getString(R.string.total_time)).append(" ").append(formattedDuration).append("\n\n");
        }

        textView.setText(stringBuilder.toString());
    }

    //function for searchbar filtration realization
    private List<ContactStatistics> filterContactStatistics(List<ContactStatistics> list, String searchText) {
        List<ContactStatistics> filteredList = new ArrayList<>();

        for (ContactStatistics stats : list) {
            if (stats.getContactName().toLowerCase().contains(searchText) ||
                    stats.getPhoneNumber().contains(searchText)) {
                filteredList.add(stats);
            }
        }

        return filteredList;
    }


    //Filter button functional realization
    private void showFilterOptions() {

        //Popup menu declaration
        PopupMenu popupMenu = new PopupMenu(this, filterButton);
        popupMenu.getMenuInflater().inflate(R.menu.filter_options_popup, popupMenu.getMenu());

        // Find the last checked filter radio button and set check on it
        MenuItem lastChecked = popupMenu.getMenu().findItem(lastCheckedId);
        lastChecked.setChecked(true);

        popupMenu.setOnMenuItemClickListener(item -> {
            // Remove check from radio button  if another button is selected
            if (lastCheckedId != item.getItemId()) {
                MenuItem previouslyChecked = popupMenu.getMenu().findItem(lastCheckedId);
                if (previouslyChecked != null) {
                    previouslyChecked.setChecked(false);
                }
            }

            // Statistic sort, according chosen option
            if (item.getItemId() == R.id.often_calls) {
                // Sort by Count from High to Low
                Filter.sortByCountHighToLow(contactStatisticsList);
            } else if (item.getItemId() == R.id.rarest_calls) {
                // Sort by Count from Low to High
                Filter.sortByCountLowToHigh(contactStatisticsList);
            } else if (item.getItemId() == R.id.longest_calls) {
                // Sort by Total Duration from High to Low
                Filter.sortByTotalDurationHighToLow(contactStatisticsList);
            } else if (item.getItemId() == R.id.shortest_calls) {
                // Sort by Total Duration from Low to High
                Filter.sortByTotalDurationLowToHigh(contactStatisticsList);
            }

            // Update the check
            lastCheckedId = item.getItemId();

            // Update the displayed statistics
            displayStatistics(contactStatisticsList);

            return true;
        });

        // Show the pop-up menu
        popupMenu.show();
    }

    //Convert seconds to other format
    private String formatTotalDuration(long totalDuration) {
        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        int selectedTimeFormat = preferences.getInt("selectedTimeFormat", R.id.time_seconds_radio);
        // Convert total duration based on the selected time format
        if (selectedTimeFormat == R.id.time_minutes_radio) {
            // Convert to minutes
            return convertToMinutes(totalDuration);
        } else if (selectedTimeFormat == R.id.time_hours_radio) {
            // Convert to hours
            return convertToHours(totalDuration);
        } else if (selectedTimeFormat == R.id.time_all_radio) {
            // Format as H:M:S
            return formatHoursMinutesSeconds(totalDuration);
        } else {
            // Default to seconds
            return convertToSeconds(totalDuration);
        }
    }


    // Convert seconds to minutes
    private String convertToMinutes(long seconds) {
        long minutes = seconds / 60;
        return minutes + " " + getString(R.string.minutes);
    }
    // Convert seconds to hours
    private String convertToHours(long seconds) {
        long hours = seconds / 3600;
        return hours + " " + getString(R.string.hours);
    }

    @SuppressLint("DefaultLocale")


    // Format seconds as H:M:S
    private String formatHoursMinutesSeconds(long seconds) {

        long hours = seconds / 3600;
        long remainingMinutes = (seconds % 3600) / 60;
        long remainingSeconds = seconds % 60;

        return String.format("%02d:%02d:%02d", hours, remainingMinutes, remainingSeconds);
    }
    // Convert total duration to seconds
    private String convertToSeconds(long totalDuration) {
        return totalDuration + " " + getString(R.string.seconds);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stat);

        // UI element initialization
        textView = findViewById(R.id.CallInfo);
        filterButton = findViewById(R.id.filterButton);
        EditText searchBar = findViewById(R.id.searchField);
        backButton = findViewById(R.id.backButton);
        // Calculate contact statistics and assign the context
        contactStatisticsList = ContactStatistics.calculateContactStatisticsList(getApplicationContext());
        displayStatistics(contactStatisticsList);


        backButton.setOnClickListener(v -> startMain(backButton));
        // Set up a TextWatcher for the search bar
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // not used, bur need
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Filter statistics, according to written text
                String searchText = charSequence.toString().toLowerCase();
                List<ContactStatistics> filteredList = filterContactStatistics(contactStatisticsList, searchText);

                // Display filtered statistics
                displayStatistics(filteredList);
            }

            @Override

            public void afterTextChanged(Editable editable) {
                // Not used, but required for implementation
            }
        });

        filterButton.setOnClickListener(v -> showFilterOptions());
    }

}


