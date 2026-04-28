//package com.example.safe_now_2;
//
//import android.Manifest;
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.content.pm.PackageManager;
//import android.database.Cursor;
//import android.location.Location;
//import android.location.LocationListener;
//import android.location.LocationManager;
//import android.net.Uri;
//import android.os.Bundle;
//import android.provider.ContactsContract;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.app.ActivityCompat;
//import androidx.core.content.ContextCompat;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.example.safe_now_2.controller.HomeActivity;
//import com.example.safe_now_2.controller.MainActivity;
//import com.example.safe_now_2.database.AlerteUrgenceDAO;
//import com.example.safe_now_2.database.ContactUrgenceDAO;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class Contact_activity extends AppCompatActivity {
//
//    private static final int PICK_CONTACT_REQUEST = 1;
//    private static final int PERMISSION_REQUEST = 2;
//    public static final int LOCATION_REQUEST = 3;
//
//    private ContactUrgenceDAO contactDAO;
//    private AlerteUrgenceDAO alerteDAO;
//    private RecyclerView recyclerView;
//    private ContactAdapter adapter;
//    private List<ContactAdapter.ContactItem> contactList = new ArrayList<>();
//
//    private SharedPreferences prefs;
//    private int UTILISATEUR_ID = 1;
//
//    private TextView tvCoordinates;
//    private LocationManager locationManager;
//    private LocationListener locationListener;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_contact);
//
//        // Initialisation
//        prefs = getSharedPreferences("user_session", MODE_PRIVATE);
//        String userIdStr = prefs.getString("user_id", "1");
//        try {
//            UTILISATEUR_ID = Integer.parseInt(userIdStr);
//        } catch (NumberFormatException e) {
//            UTILISATEUR_ID = 1;
//        }
//
//        setupBottomNav();
//        contactDAO = new ContactUrgenceDAO(this);
//        alerteDAO = new AlerteUrgenceDAO(this); // ✅ SAUVEGARDE ALERTES
//
//        tvCoordinates = findViewById(R.id.tv_coordinates);
//
//        // ===================== EMERGENCY CALL BUTTONS ✅ SAUVEGARDE =====================
//        findViewById(R.id.btn_call_police).setOnClickListener(v ->
//                appelUrgenceEtSauvegarder("19", "Police", "POLICE"));
//
//        findViewById(R.id.btn_call_ambulance).setOnClickListener(v ->
//                appelUrgenceEtSauvegarder("15", "Ambulance", "AMBULANCE"));
//
//        findViewById(R.id.btn_call_fire).setOnClickListener(v ->
//                appelUrgenceEtSauvegarder("18", "Pompiers", "POMPIERS"));
//
//        // ===================== RECYCLERVIEW =====================
//        recyclerView = findViewById(R.id.recycler_contacts);
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//        recyclerView.setNestedScrollingEnabled(false);
//
//        adapter = new ContactAdapter(this, contactList, (id, nom) -> {
//            contactDAO.delete(id);
//            chargerContacts();
//            Toast.makeText(this, nom + " supprimé", Toast.LENGTH_SHORT).show();
//        });
//
//        recyclerView.setAdapter(adapter);
//        chargerContacts();
//
//        // ===================== ADD CONTACT =====================
//        TextView btn_add = findViewById(R.id.btn_add);
//        btn_add.setOnClickListener(v -> {
//            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
//                    != PackageManager.PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions(this,
//                        new String[]{Manifest.permission.READ_CONTACTS},
//                        PERMISSION_REQUEST);
//            } else {
//                ouvrirContacts();
//            }
//        });
//
//        obtenirLocalisation();
//    }
//
//    // ===================== APPEL URGENCE + SAUVEGARDE ✅ =====================
//    private void appelUrgenceEtSauvegarder(String numero, String libelle, String type) {
//        String localisation = tvCoordinates.getText().toString();
//
//        // ✅ SAUVEGARDER L'APPEL
//        long idAlerte = alerteDAO.insertAppelUrgence(numero, type, UTILISATEUR_ID, localisation);
//
//        // Lancer l'appel
//        try {
//            Intent intent = new Intent(Intent.ACTION_DIAL);
//            intent.setData(Uri.parse("tel:" + numero));
//            startActivity(intent);
//
//            Toast.makeText(this,
//                    "🚨 Appel " + libelle + " (" + numero + ")\n✅ Sauvegardé #" + idAlerte,
//                    Toast.LENGTH_LONG).show();
//
//        } catch (Exception e) {
//            // Marquer comme échoué
//            alerteDAO.updateStatut((int) idAlerte, "ECHEC");
//            Toast.makeText(this, "Erreur appel " + numero, Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    // ===================== SMS SOS + SAUVEGARDE ✅ PRINCIPAL =====================
//    public void envoyerSOSAUnContact(String telephone, String nom) {
//        if (telephone == null || telephone.trim().isEmpty()) {
//            Toast.makeText(this, "Numéro invalide", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        String localisation = tvCoordinates.getText().toString();
//
//        // Préparer message SOS
//        String message = "🚨 EMERGENCY SOS! " + nom + "\n\n";
//        message += "I AM IN DANGER!\n\n";
//        if (!localisation.isEmpty() && !localisation.equals("00.000000,00.000000")) {
//            message += "📍 LOCATION: https://maps.google.com/?q=" + localisation + "\n";
//        }
//        message += "CALL ME IMMEDIATELY!\nAutomated SOS.";
//
//        // ✅ SAUVEGARDER LE SMS
//        long idAlerte = alerteDAO.insertSmsUrgence(nom, telephone, UTILISATEUR_ID, localisation, message);
//
//        // Ouvrir SMS avec message pré-rempli
//        try {
//            Intent smsIntent = new Intent(Intent.ACTION_VIEW);
//            smsIntent.setData(Uri.parse("smsto:" + telephone.trim()));
//            smsIntent.putExtra("sms_body", message);
//            smsIntent.putExtra("exit_on_sent", true);
//
//            startActivity(smsIntent);
//
//            Toast.makeText(this,
//                    "🚨 SOS SMS à " + nom + "\n📱 Message prêt! Appuyez SEND\n✅ Sauvegardé #" + idAlerte,
//                    Toast.LENGTH_LONG).show();
//
//        } catch (Exception e) {
//            alerteDAO.updateStatut((int) idAlerte, "ECHEC");
//            Toast.makeText(this, "Erreur SMS", Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    // ===================== CONTACTS =====================
//    private void chargerContacts() {
//        contactList.clear();
//        Cursor cursor = contactDAO.getByUtilisateur(UTILISATEUR_ID);
//
//        if (cursor != null) {
//            if (cursor.moveToFirst()) {
//                do {
//                    try {
//                        int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
//                        String nom = cursor.getString(cursor.getColumnIndexOrThrow("nom"));
//                        String tel = cursor.getString(cursor.getColumnIndexOrThrow("telephone"));
//                        if (nom != null && tel != null) {
//                            contactList.add(new ContactAdapter.ContactItem(id, nom, tel));
//                        }
//                    } catch (Exception e) {}
//                } while (cursor.moveToNext());
//            }
//            cursor.close();
//        }
//        adapter.notifyDataSetChanged();
//    }
//
//    private void ouvrirContacts() {
//        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
//        startActivityForResult(intent, PICK_CONTACT_REQUEST);
//    }
//
//    // ===================== LOCATION =====================
//    private void obtenirLocalisation() {
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED &&
//                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
//                        != PackageManager.PERMISSION_GRANTED) {
//
//            ActivityCompat.requestPermissions(this,
//                    new String[]{
//                            Manifest.permission.ACCESS_FINE_LOCATION,
//                            Manifest.permission.ACCESS_COARSE_LOCATION
//                    },
//                    LOCATION_REQUEST);
//            return;
//        }
//
//        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
//        locationListener = location -> afficherCoordonnees(location);
//
//        try {
//            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
//                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);
//            }
//            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
//                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 10, locationListener);
//            }
//        } catch (Exception e) {
//            Toast.makeText(this, "Erreur localisation", Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    private void afficherCoordonnees(Location location) {
//        if (location != null) {
//            double lat = location.getLatitude();
//            double lng = location.getLongitude();
//            String coordonnees = String.format("%.6f,%.6f", lat, lng);
//            tvCoordinates.setText(coordonnees);
//        }
//    }
//
//    // ===================== PERMISSIONS =====================
//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//
//        if (requestCode == PERMISSION_REQUEST && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//            ouvrirContacts();
//        } else if (requestCode == LOCATION_REQUEST && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//            obtenirLocalisation();
//        }
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == PICK_CONTACT_REQUEST && resultCode == RESULT_OK && data != null) {
//            Uri contactUri = data.getData();
//            if (contactUri != null) {
//                String contactId = null, nom = "", telephone = "";
//                try (Cursor cursor = getContentResolver().query(contactUri, null, null, null, null)) {
//                    if (cursor != null && cursor.moveToFirst()) {
//                        contactId = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
//                        nom = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));
//                    }
//                    if (contactId != null) {
//                        try (Cursor phoneCursor = getContentResolver().query(
//                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
//                                null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?", new String[]{contactId}, null)) {
//                            if (phoneCursor != null && phoneCursor.moveToFirst()) {
//                                telephone = phoneCursor.getString(phoneCursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
//                            }
//                        }
//                    }
//                }
//                if (!nom.trim().isEmpty() && !telephone.trim().isEmpty()) {
//                    contactDAO.insert(nom.trim(), telephone.trim(), UTILISATEUR_ID);
//                    chargerContacts();
//                    Toast.makeText(this, nom + " ajouté", Toast.LENGTH_SHORT).show();
//                }
//            }
//        }
//    }
//
//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        if (locationManager != null && locationListener != null) {
//            try {
//                locationManager.removeUpdates(locationListener);
//            } catch (Exception e) {}
//        }
//    }
//
//    private void setupBottomNav() {
//        findViewById(R.id.nav_home).setOnClickListener(v -> {
//            startActivity(new Intent(this, HomeActivity.class));
//            finish();
//        });
//        findViewById(R.id.nav_contacts).setOnClickListener(v -> {});
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
//}



package com.example.safe_now_2;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.safe_now_2.controller.HomeActivity;
import com.example.safe_now_2.controller.MainActivity;
import com.example.safe_now_2.database.AlerteUrgenceDAO;
import com.example.safe_now_2.database.ContactUrgenceDAO;

import java.util.ArrayList;
import java.util.List;

public class Contact_activity extends AppCompatActivity {

    private static final int PICK_CONTACT_REQUEST = 1;
    private static final int PERMISSION_REQUEST = 2;
    public static final int LOCATION_REQUEST = 3;

    private ContactUrgenceDAO contactDAO;
    private AlerteUrgenceDAO alerteDAO;
    private RecyclerView recyclerView;
    private ContactAdapter adapter;
    private List<ContactAdapter.ContactItem> contactList = new ArrayList<>();

    private SharedPreferences prefs;
    private int UTILISATEUR_ID = 1;

    private TextView tvCoordinates;
    private LocationManager locationManager;
    private LocationListener locationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        // Initialisation
        prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        int userIdStr = prefs.getInt("user_id", 1);
        try {
            UTILISATEUR_ID = userIdStr;
        } catch (NumberFormatException e) {
            UTILISATEUR_ID = 1;
        }

        setupBottomNav();
        contactDAO = new ContactUrgenceDAO(this);
        alerteDAO = new AlerteUrgenceDAO(this); // ✅ SAUVEGARDE ALERTES

        tvCoordinates = findViewById(R.id.tv_coordinates);

        // ===================== EMERGENCY CALL BUTTONS ✅ SAUVEGARDE =====================
        findViewById(R.id.btn_call_police).setOnClickListener(v ->
                appelUrgenceEtSauvegarder("19", "Police", "POLICE"));

        findViewById(R.id.btn_call_ambulance).setOnClickListener(v ->
                appelUrgenceEtSauvegarder("15", "Ambulance", "AMBULANCE"));

        findViewById(R.id.btn_call_fire).setOnClickListener(v ->
                appelUrgenceEtSauvegarder("18", "Pompiers", "POMPIERS"));

        // ===================== RECYCLERVIEW =====================
        recyclerView = findViewById(R.id.recycler_contacts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setNestedScrollingEnabled(false);

        adapter = new ContactAdapter(this, contactList, (id, nom) -> {
            contactDAO.delete(id);
            chargerContacts();
            Toast.makeText(this, nom + " supprimé", Toast.LENGTH_SHORT).show();
        });

        recyclerView.setAdapter(adapter);
        chargerContacts();

        // ===================== ADD CONTACT =====================
        TextView btn_add = findViewById(R.id.btn_add);
        btn_add.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_CONTACTS},
                        PERMISSION_REQUEST);
            } else {
                ouvrirContacts();
            }
        });

        obtenirLocalisation();
    }

    // ===================== APPEL URGENCE + SAUVEGARDE ✅ =====================
    private void appelUrgenceEtSauvegarder(String numero, String libelle, String type) {
        String localisation = tvCoordinates.getText().toString();

        // ✅ SAUVEGARDER L'APPEL
        long idAlerte = alerteDAO.insertAppelUrgence(numero, type, UTILISATEUR_ID, localisation);

        // Lancer l'appel
        try {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + numero));
            startActivity(intent);

            Toast.makeText(this,
                    "🚨 Appel " + libelle + " (" + numero + ")\n✅ Sauvegardé #" + idAlerte,
                    Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            // Marquer comme échoué
            alerteDAO.updateStatut((int) idAlerte, "ECHEC");
            Toast.makeText(this, "Erreur appel " + numero, Toast.LENGTH_SHORT).show();
        }
    }

    // ===================== SMS SOS + SAUVEGARDE ✅ PRINCIPAL =====================
    public void envoyerSOSAUnContact(String telephone, String nom) {
        if (telephone == null || telephone.trim().isEmpty()) {
            Toast.makeText(this, "Numéro invalide", Toast.LENGTH_SHORT).show();
            return;
        }

        String localisation = tvCoordinates.getText().toString();

        // Préparer message SOS
        String message = "🚨 EMERGENCY SOS! " + nom + "\n\n";
        message += "I AM IN DANGER!\n\n";
        if (!localisation.isEmpty() && !localisation.equals("00.000000,00.000000")) {
            message += "📍 LOCATION: https://maps.google.com/?q=" + localisation + "\n";
        }
        message += "CALL ME IMMEDIATELY!\nAutomated SOS.";

        // ✅ SAUVEGARDER LE SMS
        long idAlerte = alerteDAO.insertSmsUrgence(nom, telephone, UTILISATEUR_ID, localisation, message);

        // Ouvrir SMS avec message pré-rempli
        try {
            Intent smsIntent = new Intent(Intent.ACTION_VIEW);
            smsIntent.setData(Uri.parse("smsto:" + telephone.trim()));
            smsIntent.putExtra("sms_body", message);
            smsIntent.putExtra("exit_on_sent", true);

            startActivity(smsIntent);

            Toast.makeText(this,
                    "🚨 SOS SMS à " + nom + "\n📱 Message prêt! Appuyez SEND\n✅ Sauvegardé #" + idAlerte,
                    Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            alerteDAO.updateStatut((int) idAlerte, "ECHEC");
            Toast.makeText(this, "Erreur SMS", Toast.LENGTH_SHORT).show();
        }
    }

    // ===================== CONTACTS =====================
    private void chargerContacts() {
        contactList.clear();
        Cursor cursor = contactDAO.getByUtilisateur(UTILISATEUR_ID);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    try {
                        int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                        String nom = cursor.getString(cursor.getColumnIndexOrThrow("nom"));
                        String tel = cursor.getString(cursor.getColumnIndexOrThrow("telephone"));
                        if (nom != null && tel != null) {
                            contactList.add(new ContactAdapter.ContactItem(id, nom, tel));
                        }
                    } catch (Exception e) {}
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        adapter.notifyDataSetChanged();
    }

    private void ouvrirContacts() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, PICK_CONTACT_REQUEST);
    }

    // ===================== LOCATION =====================
    private void obtenirLocalisation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    LOCATION_REQUEST);
            return;
        }

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = location -> afficherCoordonnees(location);

        try {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);
            }
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 10, locationListener);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Erreur localisation", Toast.LENGTH_SHORT).show();
        }
    }

    private void afficherCoordonnees(Location location) {
        if (location != null) {
            double lat = location.getLatitude();
            double lng = location.getLongitude();
            String coordonnees = String.format("%.6f,%.6f", lat, lng);
            tvCoordinates.setText(coordonnees);
        }
    }

    // ===================== PERMISSIONS =====================
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            ouvrirContacts();
        } else if (requestCode == LOCATION_REQUEST && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            obtenirLocalisation();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_CONTACT_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri contactUri = data.getData();
            if (contactUri != null) {
                String contactId = null, nom = "", telephone = "";
                try (Cursor cursor = getContentResolver().query(contactUri, null, null, null, null)) {
                    if (cursor != null && cursor.moveToFirst()) {
                        contactId = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
                        nom = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));
                    }
                    if (contactId != null) {
                        try (Cursor phoneCursor = getContentResolver().query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?", new String[]{contactId}, null)) {
                            if (phoneCursor != null && phoneCursor.moveToFirst()) {
                                telephone = phoneCursor.getString(phoneCursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            }
                        }
                    }
                }
                if (!nom.trim().isEmpty() && !telephone.trim().isEmpty()) {
                    contactDAO.insert(nom.trim(), telephone.trim(), UTILISATEUR_ID);
                    chargerContacts();
                    Toast.makeText(this, nom + " ajouté", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationManager != null && locationListener != null) {
            try {
                locationManager.removeUpdates(locationListener);
            } catch (Exception e) {}
        }
    }

    private void setupBottomNav() {
        findViewById(R.id.nav_home).setOnClickListener(v -> {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
        });
        findViewById(R.id.nav_contacts).setOnClickListener(v -> {});
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