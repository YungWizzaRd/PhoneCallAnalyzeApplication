package com.example.analyzer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import java.util.Locale;

public class Settings extends AppCompatActivity {
    private ImageButton backButton;
    private RadioGroup language;
    private int selectedTimeFormat = R.id.time_seconds_radio;
    private int locale = R.id.englishRadio;

    //function for back button
    public void startMain(View v){
        Intent intent= new Intent(this, MainActivity.class );
        startActivity(intent);
    }

    // locale set
    private void setLocale(String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Resources resources = getResources();
        Configuration configuration = resources.getConfiguration();
        configuration.setLocale(locale);

        resources.updateConfiguration(configuration, resources.getDisplayMetrics());

        Intent refreshIntent = getIntent();
        finish();
        startActivity(refreshIntent);
    }

    //Language change realization
    private void setLanguageBasedOnRadioSelection() {
        language = findViewById(R.id.language);
        int selectedId = language.getCheckedRadioButtonId();

        if (selectedId == R.id.englishRadio) {
            setLocale("en"); // Change locale to English
        } else if (selectedId == R.id.latvianRadio) {
            setLocale("lv"); // Change locale to Latvian
        } else if (selectedId == R.id.russianRadio) {
            setLocale("ru"); // Change locale to Russian
        } else {
            // Default language
            setLocale("en");
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // UI element initialization
        RadioGroup timeFormat = findViewById(R.id.timeFormatGroup);
        backButton = findViewById(R.id.backButton);
        androidx.appcompat.widget.SwitchCompat nightModeSwitch = findViewById(R.id.nightModeSwitch);
        language = findViewById(R.id.language);

        // Get information about chosen earlier time format radio button
        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        int savedTimeFormatId = preferences.getInt("selectedTimeFormat", R.id.time_seconds_radio);
        timeFormat.check(savedTimeFormatId);

        // Get information about chosen earlier language radio button
        int savedLanguage= preferences.getInt("locale", R.id.englishRadio);
        language.check(savedLanguage);


        // Retrieve the current system night mode setting
        int nightModeFlags = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        boolean isNightMode = nightModeFlags == Configuration.UI_MODE_NIGHT_YES;

        // Move switch in the right position, according to the phone UI settings
        nightModeSwitch.setChecked(isNightMode);



        //language RadioButton realization
        language.setOnCheckedChangeListener((group, checkedId) -> {
            //change language
            setLanguageBasedOnRadioSelection();
            locale = checkedId;
            // Saves chosen language to Shared preferences
            SharedPreferences preferences1 = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences1.edit();
            editor.putInt("locale", locale);
            editor.apply();
        });


        //Back button realization
        backButton.setOnClickListener(v -> startMain(backButton));

        //Time format RadioButton realization
        timeFormat.setOnCheckedChangeListener((group, checkedId) -> {
            // Save the selected time format ID to SharedPreferences
            selectedTimeFormat = checkedId;
            // Saves chosen time format to Shared preferences
            SharedPreferences preferences12 = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences12.edit();
            editor.putInt("selectedTimeFormat", selectedTimeFormat);
            editor.apply();
        });


        // Night mode switch realization
        nightModeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Toggle night mode based on the switch state
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
            recreate(); // to apply night mode need to recreate activity
        });
    }


}