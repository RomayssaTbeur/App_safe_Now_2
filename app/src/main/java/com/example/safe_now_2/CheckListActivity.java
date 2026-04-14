package com.example.safe_now_2;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.safe_now_2.controller.HomeActivity;
import com.example.safe_now_2.controller.MainActivity;
import com.example.safe_now_2.controller.SplashActivity;

public class CheckListActivity extends AppCompatActivity {

    private static final int LOCATION_REQUEST = 3;

    // Scenario cards
    CardView scenarioFire, scenarioEarthquake, scenarioAggression;

    // Checklist
    CheckBox check1, check2, check3, check4;
    Button btnReset;

    // Step TextViews
    TextView step1Title, step1Sub;
    TextView step2Title, step2Sub;
    TextView step3Title, step3Sub;
    TextView step4Title, step4Sub;

    // Location — on n'utilise que tv_location_status
    TextView tvLocationStatus;
    LocationManager locationManager;
    LocationListener locationListener;

    // Active scenario
    String activeScenario = "fire";

    // ─── Scenario steps data ─────────────────────────────────
    private static final String[][] STEPS_FIRE = {
            {"Leave the building",      "Follow the nearest green exit sign immediately"},
            {"Do not use elevators",    "Elevators may lose power or trap passengers"},
            {"Call emergency services", "Dial 15 or local emergency once you are safe"},
            {"Alert neighbors",         "If possible, knock on doors as you exit"}
    };

    private static final String[][] STEPS_EARTHQUAKE = {
            {"Drop, Cover, Hold On",    "Get under a sturdy table and protect your head"},
            {"Stay away from windows",  "Glass and falling objects are the main danger"},
            {"Do not run outside",      "Wait until shaking stops before evacuating"},
            {"Check for injuries",      "Help others once it is safe to move"}
    };

    private static final String[][] STEPS_AGGRESSION = {
            {"Stay calm and alert",     "Do not provoke or challenge the aggressor"},
            {"Find a safe exit",        "Quietly move toward the nearest exit if possible"},
            {"Call security or police", "Dial 19 or alert building security immediately"},
            {"Lock yourself in",        "If exit is blocked, lock the door and stay silent"}
    };

    // ─────────────────────────────────────────────────────────

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.checklist);
        setupBottomNav();
        bindViews();
        setupListeners();
        activateScenario("fire");
        obtenirLocalisation();
    }

    // ─── View binding ────────────────────────────────────────

    private void bindViews() {
        scenarioFire       = findViewById(R.id.scenario_fire);
        scenarioEarthquake = findViewById(R.id.scenario_earthquake);
        scenarioAggression = findViewById(R.id.scenario_aggression);

        check1 = findViewById(R.id.check1);
        check2 = findViewById(R.id.check2);
        check3 = findViewById(R.id.check3);
        check4 = findViewById(R.id.check4);

        btnReset = findViewById(R.id.btn_reset);

        step1Title = findViewById(R.id.step1_title); step1Sub = findViewById(R.id.step1_sub);
        step2Title = findViewById(R.id.step2_title); step2Sub = findViewById(R.id.step2_sub);
        step3Title = findViewById(R.id.step3_title); step3Sub = findViewById(R.id.step3_sub);
        step4Title = findViewById(R.id.step4_title); step4Sub = findViewById(R.id.step4_sub);

        // Seul TextView de localisation utilisé
        tvLocationStatus = findViewById(R.id.tv_location_status);
    }

    // ─── Listeners ───────────────────────────────────────────

    private void setupListeners() {
        btnReset.setOnClickListener(v -> resetChecklist());
        scenarioFire.setOnClickListener(v       -> activateScenario("fire"));
        scenarioEarthquake.setOnClickListener(v -> activateScenario("earthquake"));
        scenarioAggression.setOnClickListener(v -> activateScenario("aggression"));
    }

    // ─── Scenario switching ──────────────────────────────────

    private void activateScenario(String scenario) {
        activeScenario = scenario;
        resetChecklist();

        float activeElev   = dpToPx(8);
        float inactiveElev = dpToPx(2);

        scenarioFire.setAlpha(0.55f);       scenarioFire.setCardElevation(inactiveElev);
        scenarioEarthquake.setAlpha(0.55f); scenarioEarthquake.setCardElevation(inactiveElev);
        scenarioAggression.setAlpha(0.55f); scenarioAggression.setCardElevation(inactiveElev);

        switch (scenario) {
            case "fire":
                scenarioFire.setAlpha(1f);
                scenarioFire.setCardElevation(activeElev);
                loadSteps(STEPS_FIRE);
                break;
            case "earthquake":
                scenarioEarthquake.setAlpha(1f);
                scenarioEarthquake.setCardElevation(activeElev);
                loadSteps(STEPS_EARTHQUAKE);
                break;
            case "aggression":
                scenarioAggression.setAlpha(1f);
                scenarioAggression.setCardElevation(activeElev);
                loadSteps(STEPS_AGGRESSION);
                break;
        }
    }

    private void loadSteps(String[][] steps) {
        step1Title.setText(steps[0][0]); step1Sub.setText(steps[0][1]);
        step2Title.setText(steps[1][0]); step2Sub.setText(steps[1][1]);
        step3Title.setText(steps[2][0]); step3Sub.setText(steps[2][1]);
        step4Title.setText(steps[3][0]); step4Sub.setText(steps[3][1]);
    }

    // ─── Reset ───────────────────────────────────────────────

    private void resetChecklist() {
        check1.setChecked(false);
        check2.setChecked(false);
        check3.setChecked(false);
        check4.setChecked(false);
    }

    // ─── Localisation ────────────────────────────────────────

    private void obtenirLocalisation() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_REQUEST);
            return;
        }

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                afficherCoordonnees(location);
            }
        };

        // GPS provider
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, 5000, 10, locationListener);

            Location last = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (last != null) afficherCoordonnees(last);
        }

        // Network provider (plus rapide au démarrage)
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, 5000, 10, locationListener);

            Location last = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (last != null) afficherCoordonnees(last);
        }

        // Aucune position encore connue
        if (tvLocationStatus.getText().toString().equals("YOUR LIVE LOCATION: ACTIVE")) {
            return;
        }
        tvLocationStatus.setText("Localisation en cours...");
    }

    private void afficherCoordonnees(Location location) {
        double lat = location.getLatitude();
        double lng = location.getLongitude();

        // Les coordonnées remplacent directement le texte du live location TextView
        String coordonnees = String.format("%.4f° %s,  %.4f° %s",
                Math.abs(lat), lat >= 0 ? "N" : "S",
                Math.abs(lng), lng >= 0 ? "E" : "W");

        tvLocationStatus.setText(coordonnees);
    }

    // ─── Permissions ─────────────────────────────────────────

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == LOCATION_REQUEST) {
                obtenirLocalisation();
            }
        } else {
            tvLocationStatus.setText("Permission refusée");
        }
    }

    // ─── Nettoyage ───────────────────────────────────────────

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationManager != null && locationListener != null) {
            locationManager.removeUpdates(locationListener);
        }
    }

    // ─── Utility ─────────────────────────────────────────────

    private float dpToPx(int dp) {
        return dp * getResources().getDisplayMetrics().density;
    }
    private void setupBottomNav() {

        findViewById(R.id.nav_home).setOnClickListener(v -> {
            startActivity(new Intent(this, HomeActivity.class));
        });

        findViewById(R.id.nav_contacts).setOnClickListener(v -> {
            startActivity(new Intent(this, Contact_activity.class));
        });

        findViewById(R.id.nav_sos).setOnClickListener(v -> {
            startActivity(new Intent(this, HomeActivity.class));
        });

        findViewById(R.id.nav_history).setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
        });

        findViewById(R.id.nav_checklist).setOnClickListener(v -> {

        });
    }
}
