package com.example.safe_now_2;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
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

import com.example.safe_now_2.database.ContactUrgenceDAO;

import java.util.ArrayList;
import java.util.List;

public class Contact_activity extends AppCompatActivity {

    private static final int PICK_CONTACT_REQUEST = 1;
    private static final int PERMISSION_REQUEST = 2;
    private static final int LOCATION_REQUEST = 3;
    private static final int UTILISATEUR_ID = 1;

    private ContactUrgenceDAO contactDAO;
    private RecyclerView recyclerView;
    private ContactAdapter adapter;
    private List<ContactAdapter.ContactItem> contactList = new ArrayList<>();

    private TextView tvCoordinates;
    private LocationManager locationManager;
    private LocationListener locationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        contactDAO = new ContactUrgenceDAO(this);
        tvCoordinates = findViewById(R.id.tv_coordinates);

        // ===================== CALL BUTTONS =====================
        findViewById(R.id.btn_call_police)
                .setOnClickListener(v -> appelerNumero("19"));

        findViewById(R.id.btn_call_ambulance)
                .setOnClickListener(v -> appelerNumero("15"));

        findViewById(R.id.btn_call_fire)
                .setOnClickListener(v -> appelerNumero("150"));

        // ===================== SMS BUTTON =====================
        if (findViewById(R.id.btn_chat) != null) {
            findViewById(R.id.btn_chat).setOnClickListener(v -> {
                envoyerSOSATousLesContacts();
            });
        }

        // ===================== RECYCLER VIEW =====================
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
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_CONTACTS)
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

    // ===================== SMS SOS =====================
    private void envoyerSOSATousLesContacts() {

        if (contactList == null || contactList.isEmpty()) {
            Toast.makeText(this, "Aucun contact disponible", Toast.LENGTH_SHORT).show();
            return;
        }

        String localisation = tvCoordinates.getText().toString();

        String message = "🚨 HELP ! Je suis en danger !\n";

        if (localisation != null && !localisation.isEmpty()) {
            message += "📍 https://maps.google.com/?q=" + localisation;
        }

        StringBuilder numeros = new StringBuilder();

        for (ContactAdapter.ContactItem contact : contactList) {
            if (contact.telephone != null && !contact.telephone.isEmpty()) {
                numeros.append(contact.telephone).append(";");
            }
        }

        if (numeros.length() == 0) {
            Toast.makeText(this, "Aucun numéro valide", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("smsto:" + numeros.toString()));
        intent.putExtra("sms_body", message);

        startActivity(intent);
    }

    // ===================== CALL =====================
    private void appelerNumero(String numero) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + numero));
        startActivity(intent);
    }

    public void appelerContact(String numero) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + numero));
        startActivity(intent);
    }

    // ===================== CONTACTS =====================
    private void chargerContacts() {
        contactList.clear();
        Cursor cursor = contactDAO.getByUtilisateur(UTILISATEUR_ID);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String nom = cursor.getString(cursor.getColumnIndexOrThrow("nom"));
                String tel = cursor.getString(cursor.getColumnIndexOrThrow("telephone"));

                contactList.add(new ContactAdapter.ContactItem(id, nom, tel));

            } while (cursor.moveToNext());
            cursor.close();
        }

        adapter.notifyDataSetChanged();
    }

    private void ouvrirContacts() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, PICK_CONTACT_REQUEST);
    }

    // ===================== LOCALISATION =====================
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

        locationListener = location -> afficherCoordonnees(location);

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, 5000, 10, locationListener);
        }

        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, 5000, 10, locationListener);
        }
    }

    private void afficherCoordonnees(Location location) {
        double lat = location.getLatitude();
        double lng = location.getLongitude();

        String coordonnees = lat + "," + lng; // 🔥 format Google Maps

        tvCoordinates.setText(coordonnees);
    }

    // ===================== PERMISSIONS =====================
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            if (requestCode == PERMISSION_REQUEST) {
                ouvrirContacts();

            } else if (requestCode == LOCATION_REQUEST) {
                obtenirLocalisation();
            }
        }
    }

    // ===================== RESULT CONTACT =====================
    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode,
                                    Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_CONTACT_REQUEST
                && resultCode == RESULT_OK
                && data != null) {

            Uri contactUri = data.getData();
            String contactId = null;
            String nom = "";
            String telephone = "";

            Cursor cursor = getContentResolver().query(
                    contactUri, null, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                contactId = cursor.getString(
                        cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
                nom = cursor.getString(
                        cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));
                cursor.close();
            }

            if (contactId != null) {
                Cursor phoneCursor = getContentResolver().query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?",
                        new String[]{contactId},
                        null);

                if (phoneCursor != null && phoneCursor.moveToFirst()) {
                    telephone = phoneCursor.getString(
                            phoneCursor.getColumnIndexOrThrow(
                                    ContactsContract.CommonDataKinds.Phone.NUMBER));
                    phoneCursor.close();
                }
            }

            if (!nom.isEmpty() && !telephone.isEmpty()) {
                contactDAO.insert(nom, telephone, UTILISATEUR_ID);
                chargerContacts();

                Toast.makeText(this,
                        nom + " ajouté",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    // ===================== CLEAN =====================
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationManager != null && locationListener != null) {
            locationManager.removeUpdates(locationListener);
        }
    }
}