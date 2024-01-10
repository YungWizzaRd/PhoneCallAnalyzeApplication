package com.example.analyzer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    private Button stat;
    private Button settings, exit;

    //Open statistics page
    public void startStat(View v){
        Intent intent= new Intent(this, Stat.class );
        startActivity(intent);
    }
    //Open settings  page
    public void startSettings(View v){
        Intent intent= new Intent(this, Settings.class );
        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    // UI element initialization
        stat= findViewById(R.id.stat);
        settings= findViewById(R.id.settings);
        exit = findViewById(R.id.exit);

        //statistics button realization
        stat.setOnClickListener(v -> startStat(stat));

        //setting button realization
        settings.setOnClickListener(v -> startSettings(settings));

        //exit button realization
        exit.setOnClickListener(v -> {
            finish();
            System.exit(0);
        });

    }

}