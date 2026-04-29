package com.example.safe_now_2.controller;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.safe_now_2.CheckListActivity;
import com.example.safe_now_2.Contact_activity;
import com.example.safe_now_2.controller.MainActivity;
import com.example.safe_now_2.R;
import com.example.safe_now_2.database.AlerteUrgenceDAO;
import com.example.safe_now_2.database.ContactUrgenceDAO;
import com.example.safe_now_2.utils.PermissionDialogHelper;
import com.example.safe_now_2.utils.PermissionHelper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.util.List;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {
    private double policeLat = 0;
    private double policeLng = 0;
    private static final int LOCATION_PERMISSION_REQUEST = 1001;

    // Session
    private SharedPreferences prefs;
    private int UTILISATEUR_ID = 1;

    // Database
    private ContactUrgenceDAO contactDAO;
    private AlerteUrgenceDAO alerteDAO;

    // Localisation
    private FusedLocationProviderClient fusedLocationClient;
    private TextView tvMapSubtitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("HomeActivity", " Démarrage HomeActivity");

        setContentView(R.layout.activity_home);
        setupBottomNav();
        // Initialisation complète
        initViews();
        initLocation();
        initEverything();
        setupNavigation();

        //  Localisation Police
        getNearestPoliceStation();
        tvMapSubtitle.setOnClickListener(v -> openNavigationToPolice());
        Log.d("HomeActivity", " HomeActivity prêt");
    }

    // ═══════════════════════════════════════════════════════════════
    // INITIALISATION
    // ═══════════════════════════════════════════════════════════════

    private void initViews() {
        tvMapSubtitle = findViewById(R.id.tv_map_subtitle);
    }

    private void initLocation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }

    private void initEverything() {
        initUserSession();
        initDatabase();
        initPermissions();
    }

    private void initUserSession() {
        prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        int userIdStr= prefs.getInt("user_id", 1);

        try {
            UTILISATEUR_ID = userIdStr;
        } catch (NumberFormatException e) {
            UTILISATEUR_ID = 1;
        }
        Log.d("HomeActivity", "👤 User ID: " + UTILISATEUR_ID);
    }

    private void initDatabase() {
        try {
            contactDAO = new ContactUrgenceDAO(this);
            alerteDAO = new AlerteUrgenceDAO(this);
            Log.d("HomeActivity", "🗄️ Database OK");
        } catch (Exception e) {
            Log.w("HomeActivity", "⚠ Database", e);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // 🚓 POLICE + ZONE (GRATUIT - Geocoder Natif)
    // ═══════════════════════════════════════════════════════════════

    private void getNearestPoliceStation() {
        if (!hasLocationPermission()) {
            requestLocationPermission();
            if (tvMapSubtitle != null) tvMapSubtitle.setText("Activez GPS");
            return;
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        findNearestPoliceNative(location.getLatitude(), location.getLongitude());
                    } else {
                        safeSetText("GPS indisponible");
                    }
                })
                .addOnFailureListener(e -> {
                    safeSetText("Recherche zone...");
                    Log.e("Police", "GPS error", e);
                });
    }

    private void findNearestPoliceNative(double lat, double lng) {
        new Thread(() -> {
            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                if (!Geocoder.isPresent()) {
                    safeSetText("❌ Geocoder non supporté");
                    return;
                }
                // Zone actuelle
                List<Address> zone = geocoder.getFromLocation(lat, lng, 1);
                String zoneName = "Zone GPS active";

                if (!zone.isEmpty()) {
                    Address addr = zone.get(0);
                    String city = addr.getLocality();
                    String region = addr.getAdminArea();
                    zoneName = (city != null ? city : "") +
                            (region != null ? ", " + region : "");
                }

                // Police proche


                String result = zoneName;
                List<Address> police = geocoder.getFromLocationName(
                        "police commissariat gendarmerie",
                        3,
                        lat - 0.05,
                        lng - 0.05,
                        lat + 0.05,
                        lng + 0.05
                );

                if (police != null && !police.isEmpty()) {
                    Address p = police.get(0);

                    policeLat = p.getLatitude();
                    policeLng = p.getLongitude();

                    String name = p.getFeatureName();
                    String addr = p.getAddressLine(0);

                    result = (name != null ? name : "Police") +
                            "\n" + (addr != null ? addr : zoneName);
                } else {
                    policeLat = 0;
                    policeLng = 0;
                }

                safeSetText(result);

            } catch (Exception e) {
                safeSetText("Zone GPS active");
            }
        }).start();
    }

    private void safeSetText(String text) {
        runOnUiThread(() -> {
            if (tvMapSubtitle != null) {
                tvMapSubtitle.setText(text);
            }
        });
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                }, LOCATION_PERMISSION_REQUEST);
    }

    // ═══════════════════════════════════════════════════════════════
    // NAVIGATION COMPLÈTE
    // ═══════════════════════════════════════════════════════════════

    private void setupNavigation() {
        // Cards
        safeClick(R.id.btnSOS, this::triggerSOS);
        safeClick(R.id.btnContacts, v -> startActivity(new Intent(this, Contact_activity.class)));
        safeClick(R.id.btnHistory, v -> startActivity(new Intent(this, MainActivity.class)));
        safeClick(R.id.btnChecklist, v -> startActivity(new Intent(this, CheckListActivity.class)));
        safeClick(R.id.btnSimulation, v -> startActivity(new Intent(this, VrSimulationActivity.class)));

        // Toolbar
        setupToolbar();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            View btnBack = findViewById(R.id.btnBack);
            View btnProfile = findViewById(R.id.btnProfile);

            if (btnBack != null) btnBack.setOnClickListener(v -> finish());
            if (btnProfile != null) btnProfile.setOnClickListener(v ->
                    Toast.makeText(this, "👤 Profil", Toast.LENGTH_SHORT).show());
        }
    }

    private void safeClick(int id, View.OnClickListener listener) {
        View view = findViewById(id);
        if (view != null) {
            view.setClickable(true);
            view.setFocusable(true);
            view.setOnClickListener(listener);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // 🚨 SOS - 1er Contact
    // ═══════════════════════════════════════════════════════════════

    private void triggerSOS(View v) {
        Log.d("HomeActivity", " SOS déclenché");

        if (!PermissionHelper.allGranted(this, PermissionHelper.getCriticalActionPermissions())) {
            requestCriticalPermissions();
            return;
        }

        if (!hasEmergencyContact()) {
            Toast.makeText(this, "Aucun contact !\n👆 Contacts → Ajouter", Toast.LENGTH_LONG).show();
            return;
        }

        appelUrgent();
    }

    private boolean hasEmergencyContact() {
        if (contactDAO == null) return false;
        Cursor cursor = null;
        try {
            cursor = contactDAO.getByUtilisateur(UTILISATEUR_ID);
            return cursor != null && cursor.getCount() > 0;
        } catch (Exception e) {
            return false;
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    private void appelUrgent() {
        Cursor cursor = null;
        try {
            cursor = contactDAO.getByUtilisateur(UTILISATEUR_ID);
            cursor.moveToFirst();

            String nom = cursor.getString(cursor.getColumnIndexOrThrow("nom"));
            String numero = cursor.getString(cursor.getColumnIndexOrThrow("telephone"));

            // Sauvegarde alerte
            if (alerteDAO != null) {
                String localisation = tvMapSubtitle != null ?
                        tvMapSubtitle.getText().toString() : "GPS active";
                alerteDAO.insertAppelUrgence(numero, "SOS", UTILISATEUR_ID, localisation);
            }

            // Appel
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:" + numero));
            startActivity(intent);

            Toast.makeText(this, "🚨 " + nom + "\n📞 " + numero, Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Toast.makeText(this, " Erreur SOS: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // Permissions
    // ═══════════════════════════════════════════════════════════════

    private ActivityResultLauncher<String[]> criticalPermLauncher;

    private void initPermissions() {
        criticalPermLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                results -> {
                    boolean ok = !results.containsValue(false);
                    if (ok) {
                        Toast.makeText(this, "✅ SOS prêt", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void requestCriticalPermissions() {
        String[] perms = PermissionHelper.getCriticalActionPermissions();
        if (!PermissionHelper.allGranted(this, perms)) {
            PermissionHelper.requestPermissions(criticalPermLauncher, perms);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST)
        { getNearestPoliceStation(); }
    }
    @Override protected void onDestroy() {
        super.onDestroy(); contactDAO = null; alerteDAO = null;
    }
    private void openNavigationToPolice() {
        if (policeLat == 0 && policeLng == 0) {
            Toast.makeText(this, "Police non disponible", Toast.LENGTH_SHORT).show();
            return;
        }

        Uri gmmIntentUri = Uri.parse(
                "google.navigation:q=" + policeLat + "," + policeLng
        );

        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");

        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            // fallback navigateur
            Uri browserUri = Uri.parse(
                    "https://www.google.com/maps/dir/?api=1&destination="
                            + policeLat + "," + policeLng
            );
            startActivity(new Intent(Intent.ACTION_VIEW, browserUri));
        }
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


//package com.example.safe_now_2.controller;
//
//import android.Manifest;
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.content.pm.PackageManager;
//import android.database.Cursor;
//import android.location.Address;
//import android.location.Geocoder;
//import android.location.Location;
//import android.net.Uri;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.View;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.activity.result.ActivityResultLauncher;
//import androidx.activity.result.contract.ActivityResultContracts;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.appcompat.widget.Toolbar;
//import androidx.cardview.widget.CardView;
//import androidx.core.app.ActivityCompat;
//import androidx.core.content.ContextCompat;
//
//import com.example.safe_now_2.CheckListActivity;
//import com.example.safe_now_2.Contact_activity;
//import com.example.safe_now_2.controller.MainActivity;
//import com.example.safe_now_2.R;
//import com.example.safe_now_2.database.AlerteUrgenceDAO;
//import com.example.safe_now_2.database.ContactUrgenceDAO;
//import com.example.safe_now_2.utils.PermissionDialogHelper;
//import com.example.safe_now_2.utils.PermissionHelper;
//import com.google.android.gms.location.FusedLocationProviderClient;
//import com.google.android.gms.location.LocationServices;
//
//import java.util.List;
//import java.util.Locale;
//
//public class HomeActivity extends AppCompatActivity {
//    private double policeLat = 0;
//    private double policeLng = 0;
//    private static final int LOCATION_PERMISSION_REQUEST = 1001;
//
//    // Session
//    private SharedPreferences prefs;
//    private int UTILISATEUR_ID = 1;
//
//    // Database
//    private ContactUrgenceDAO contactDAO;
//    private AlerteUrgenceDAO alerteDAO;
//
//    // Localisation
//    private FusedLocationProviderClient fusedLocationClient;
//    private TextView tvMapSubtitle;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        Log.d("HomeActivity", " Démarrage HomeActivity");
//
//        setContentView(R.layout.activity_home);
//        setupBottomNav();
//        // Initialisation complète
//        initViews();
//        initLocation();
//        initEverything();
//        setupNavigation();
//
//        //  Localisation Police
//        getNearestPoliceStation();
//        tvMapSubtitle.setOnClickListener(v -> openNavigationToPolice());
//        Log.d("HomeActivity", " HomeActivity prêt");
//    }
//
//    // ═══════════════════════════════════════════════════════════════
//    // INITIALISATION
//    // ═══════════════════════════════════════════════════════════════
//
//    private void initViews() {
//        tvMapSubtitle = findViewById(R.id.tv_map_subtitle);
//    }
//
//    private void initLocation() {
//        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
//    }
//
//    private void initEverything() {
//        initUserSession();
//        initDatabase();
//        initPermissions();
//    }
//
//    private void initUserSession() {
//        prefs = getSharedPreferences("user_session", MODE_PRIVATE);
//        int userIdStr= prefs.getInt("user_id", -1);
//
//        try {
//            UTILISATEUR_ID = userIdStr;
//        } catch (NumberFormatException e) {
//            UTILISATEUR_ID = 1;
//        }
//        Log.d("HomeActivity", "👤 User ID: " + UTILISATEUR_ID);
//    }
//
//    private void initDatabase() {
//        try {
//            contactDAO = new ContactUrgenceDAO(this);
//            alerteDAO = new AlerteUrgenceDAO(this);
//            Log.d("HomeActivity", "🗄️ Database OK");
//        } catch (Exception e) {
//            Log.w("HomeActivity", "⚠ Database", e);
//        }
//    }
//
//    // ═══════════════════════════════════════════════════════════════
//    // 🚓 POLICE + ZONE (GRATUIT - Geocoder Natif)
//    // ═══════════════════════════════════════════════════════════════
//
//    private void getNearestPoliceStation() {
//        if (!hasLocationPermission()) {
//            requestLocationPermission();
//            if (tvMapSubtitle != null) tvMapSubtitle.setText("Activez GPS");
//            return;
//        }
//
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return;
//        }
//        fusedLocationClient.getLastLocation()
//                .addOnSuccessListener(location -> {
//                    if (location != null) {
//                        findNearestPoliceNative(location.getLatitude(), location.getLongitude());
//                    } else {
//                        safeSetText("GPS indisponible");
//                    }
//                })
//                .addOnFailureListener(e -> {
//                    safeSetText("Recherche zone...");
//                    Log.e("Police", "GPS error", e);
//                });
//    }
//
//    private void findNearestPoliceNative(double lat, double lng) {
//        new Thread(() -> {
//            try {
//                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
//                if (!Geocoder.isPresent()) {
//                    safeSetText("❌ Geocoder non supporté");
//                    return;
//                }
//                // Zone actuelle
//                List<Address> zone = geocoder.getFromLocation(lat, lng, 1);
//                String zoneName = "Zone GPS active";
//
//                if (!zone.isEmpty()) {
//                    Address addr = zone.get(0);
//                    String city = addr.getLocality();
//                    String region = addr.getAdminArea();
//                    zoneName = (city != null ? city : "") +
//                            (region != null ? ", " + region : "");
//                }
//
//                // Police proche
//
//
//                String result = zoneName;
//                List<Address> police = geocoder.getFromLocationName(
//                        "police commissariat gendarmerie",
//                        3,
//                        lat - 0.05,
//                        lng - 0.05,
//                        lat + 0.05,
//                        lng + 0.05
//                );
//
//                if (police != null && !police.isEmpty()) {
//                    Address p = police.get(0);
//
//                    policeLat = p.getLatitude();
//                    policeLng = p.getLongitude();
//
//                    String name = p.getFeatureName();
//                    String addr = p.getAddressLine(0);
//
//                    result = (name != null ? name : "Police") +
//                            "\n" + (addr != null ? addr : zoneName);
//                } else {
//                    policeLat = 0;
//                    policeLng = 0;
//                }
//
//                safeSetText(result);
//
//            } catch (Exception e) {
//                safeSetText("Zone GPS active");
//            }
//        }).start();
//    }
//
//    private void safeSetText(String text) {
//        runOnUiThread(() -> {
//            if (tvMapSubtitle != null) {
//                tvMapSubtitle.setText(text);
//            }
//        });
//    }
//
//    private boolean hasLocationPermission() {
//        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
//                == PackageManager.PERMISSION_GRANTED;
//    }
//
//    private void requestLocationPermission() {
//        ActivityCompat.requestPermissions(this,
//                new String[]{
//                        Manifest.permission.ACCESS_FINE_LOCATION,
//                        Manifest.permission.ACCESS_COARSE_LOCATION
//                }, LOCATION_PERMISSION_REQUEST);
//    }
//
//    // ═══════════════════════════════════════════════════════════════
//    // NAVIGATION COMPLÈTE
//    // ═══════════════════════════════════════════════════════════════
//
//    private void setupNavigation() {
//        // Cards
//        safeClick(R.id.btnSOS, this::triggerSOS);
//        safeClick(R.id.btnContacts, v -> startActivity(new Intent(this, Contact_activity.class)));
//        safeClick(R.id.btnHistory, v -> startActivity(new Intent(this, MainActivity.class)));
//        safeClick(R.id.btnChecklist, v -> startActivity(new Intent(this, CheckListActivity.class)));
//        safeClick(R.id.btnSimulation, v -> Toast.makeText(this, "🚀 Simulation VR", Toast.LENGTH_SHORT).show());
//
//        // Toolbar
//        setupToolbar();
//    }
//
//    private void setupToolbar() {
//        Toolbar toolbar = findViewById(R.id.toolbar);
//        if (toolbar != null) {
//            View btnBack = findViewById(R.id.btnBack);
//            View btnProfile = findViewById(R.id.btnProfile);
//
//            if (btnBack != null) btnBack.setOnClickListener(v -> finish());
//            if (btnProfile != null) btnProfile.setOnClickListener(v ->
//                    Toast.makeText(this, "👤 Profil", Toast.LENGTH_SHORT).show());
//        }
//    }
//
//    private void safeClick(int id, View.OnClickListener listener) {
//        View view = findViewById(id);
//        if (view != null) {
//            view.setClickable(true);
//            view.setFocusable(true);
//            view.setOnClickListener(listener);
//        }
//    }
//
//    // ═══════════════════════════════════════════════════════════════
//    // 🚨 SOS - 1er Contact
//    // ═══════════════════════════════════════════════════════════════
//
//    private void triggerSOS(View v) {
//        Log.d("HomeActivity", " SOS déclenché");
//
//        if (!PermissionHelper.allGranted(this, PermissionHelper.getCriticalActionPermissions())) {
//            requestCriticalPermissions();
//            return;
//        }
//
//        if (!hasEmergencyContact()) {
//            Toast.makeText(this, "Aucun contact !\n👆 Contacts → Ajouter", Toast.LENGTH_LONG).show();
//            return;
//        }
//
//        appelUrgent();
//    }
//
//    private boolean hasEmergencyContact() {
//        if (contactDAO == null) return false;
//        Cursor cursor = null;
//        try {
//            cursor = contactDAO.getByUtilisateur(UTILISATEUR_ID);
//            return cursor != null && cursor.getCount() > 0;
//        } catch (Exception e) {
//            return false;
//        } finally {
//            if (cursor != null) cursor.close();
//        }
//    }
//
//    private void appelUrgent() {
//        Cursor cursor = null;
//        try {
//            cursor = contactDAO.getByUtilisateur(UTILISATEUR_ID);
//            cursor.moveToFirst();
//
//            String nom = cursor.getString(cursor.getColumnIndexOrThrow("nom"));
//            String numero = cursor.getString(cursor.getColumnIndexOrThrow("telephone"));
//
//            // Sauvegarde alerte
//            if (alerteDAO != null) {
//                String localisation = tvMapSubtitle != null ?
//                        tvMapSubtitle.getText().toString() : "GPS active";
//                alerteDAO.insertAppelUrgence(numero, "SOS", UTILISATEUR_ID, localisation);
//            }
//
//            // Appel
//            Intent intent = new Intent(Intent.ACTION_DIAL);
//            intent.setData(Uri.parse("tel:" + numero));
//            startActivity(intent);
//
//            Toast.makeText(this, "🚨 " + nom + "\n📞 " + numero, Toast.LENGTH_LONG).show();
//
//        } catch (Exception e) {
//            Toast.makeText(this, " Erreur SOS: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//        } finally {
//            if (cursor != null) cursor.close();
//        }
//    }
//
//    // ═══════════════════════════════════════════════════════════════
//    // Permissions
//    // ═══════════════════════════════════════════════════════════════
//
//    private ActivityResultLauncher<String[]> criticalPermLauncher;
//
//    private void initPermissions() {
//        criticalPermLauncher = registerForActivityResult(
//                new ActivityResultContracts.RequestMultiplePermissions(),
//                results -> {
//                    boolean ok = !results.containsValue(false);
//                    if (ok) {
//                        Toast.makeText(this, "✅ SOS prêt", Toast.LENGTH_SHORT).show();
//                    }
//                }
//        );
//    }
//
//    private void requestCriticalPermissions() {
//        String[] perms = PermissionHelper.getCriticalActionPermissions();
//        if (!PermissionHelper.allGranted(this, perms)) {
//            PermissionHelper.requestPermissions(criticalPermLauncher, perms);
//        }
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//
//        if (requestCode == LOCATION_PERMISSION_REQUEST)
//        { getNearestPoliceStation(); }
//    }
//    @Override protected void onDestroy() {
//        super.onDestroy(); contactDAO = null; alerteDAO = null;
//    }
//    private void openNavigationToPolice() {
//        if (policeLat == 0 && policeLng == 0) {
//            Toast.makeText(this, "Police non disponible", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        Uri gmmIntentUri = Uri.parse(
//                "google.navigation:q=" + policeLat + "," + policeLng
//        );
//
//        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
//        mapIntent.setPackage("com.google.android.apps.maps");
//
//        if (mapIntent.resolveActivity(getPackageManager()) != null) {
//            startActivity(mapIntent);
//        } else {
//            // fallback navigateur
//            Uri browserUri = Uri.parse(
//                    "https://www.google.com/maps/dir/?api=1&destination="
//                            + policeLat + "," + policeLng
//            );
//            startActivity(new Intent(Intent.ACTION_VIEW, browserUri));
//        }
//    }
//    private void setupBottomNav() {
//
//        findViewById(R.id.nav_contacts).setOnClickListener(v -> {
//            startActivity(new Intent(this, Contact_activity.class));
//            finish();
//        });
//        findViewById(R.id.nav_sos).setOnClickListener(v -> {
//            startActivity(new Intent(this, HomeActivity.class));
//            finish();
//        });
//        findViewById(R.id.nav_history).setOnClickListener(v -> {
//            startActivity(new Intent(this, MainActivity.class));
//            finish();
//        });
//        findViewById(R.id.nav_checklist).setOnClickListener(v -> {
//            startActivity(new Intent(this, CheckListActivity.class));
//            finish();
//        });
//    }
//
//}
//
//
//
//
