package com.example.safe_now_2.controller;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.safe_now_2.R;
import com.example.safe_now_2.model.SimulationModel;
import com.google.android.material.button.MaterialButton;

public class VrSimulationActivity extends AppCompatActivity {

    private CardView cardFire, cardEarthquake;
    private MaterialButton btnStart;

    private SimulationModel model = new SimulationModel();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vr_simulation_activity);

        initViews();
        setupListeners();
    }

    private void initViews() {
        cardFire = findViewById(R.id.card_fire);
        cardEarthquake = findViewById(R.id.card_earthquake);
        btnStart = findViewById(R.id.btn_start_simulation);
        cardFire.setPreventCornerOverlap(true);
        cardEarthquake.setPreventCornerOverlap(true);

        cardFire.setUseCompatPadding(false);
        cardEarthquake.setUseCompatPadding(false);
    }

    private void setupListeners() {

        cardFire.setOnClickListener(v -> {
            model.setSelectedScenario(SimulationModel.Scenario.FIRE);
            updateUISelection();
        });

        cardEarthquake.setOnClickListener(v -> {
            model.setSelectedScenario(SimulationModel.Scenario.EARTHQUAKE);
            updateUISelection();
        });

        btnStart.setOnClickListener(v -> {
            if (!model.isScenarioSelected()) {
                Toast.makeText(this, "Please select a scenario", Toast.LENGTH_SHORT).show();
                return;
            }

            startSimulation();
        });
    }



    private void updateUISelection() {

        // RESET FIRE (fond + style)
        resetCard(cardFire);

        // RESET EARTHQUAKE
        resetCard(cardEarthquake);

        // APPLY SELECTION
        if (model.getSelectedScenario() == SimulationModel.Scenario.FIRE) {
            applySelectedStyle(cardFire);
        }
        else if (model.getSelectedScenario() == SimulationModel.Scenario.EARTHQUAKE) {
            applySelectedStyle(cardEarthquake);
        }
    }

    private void resetCard(CardView card) {


        card.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(0.6f)
                .setDuration(200)
                .start();

        card.setCardElevation(4f);
    }



    private void applySelectedStyle(CardView card) {

        card.setPivotX(card.getWidth() / 2f);
        card.setPivotY(card.getHeight() / 2f);

        card.animate()
                .scaleX(1.03f) // 👈 réduit un peu (important)
                .scaleY(1.03f)
                .alpha(1f)
                .setDuration(200)
                .start();

        card.setCardElevation(14f);
    }

    private void startSimulation() {

        Intent intent = new Intent(this, UnityActivity.class);
        intent.putExtra("scenario", model.getSelectedScenario().name());
        startActivity(intent);
    }
}