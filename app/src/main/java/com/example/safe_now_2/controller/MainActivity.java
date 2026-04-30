package com.example.safe_now_2.controller;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.safe_now_2.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_alert_history);
        setupBottomNav();
    }

    private void setupBottomNav() {

        findViewById(R.id.nav_contacts).setOnClickListener(v -> {
            startActivity(new Intent(this, Contact_activity.class));
            finish();
        });
        findViewById(R.id.nav_sos).setOnClickListener(v -> {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        });
        findViewById(R.id.nav_history).setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
        findViewById(R.id.nav_checklist).setOnClickListener(v -> {
            startActivity(new Intent(this, CheckListActivity.class));
            finish();
        });
    }
}