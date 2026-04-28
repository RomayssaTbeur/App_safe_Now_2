package com.example.safe_now_2.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "safenow.db";
    private static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE Utilisateur (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "nom TEXT," +
                "telephone TEXT)");

        db.execSQL("CREATE TABLE Localisation (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "latitude REAL," +
                "longitude REAL)");

        db.execSQL("CREATE TABLE AlerteUrgence (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "dateIso TEXT," +
                "statut TEXT," +
                "utilisateur_id INTEGER," +
                "localisation_id INTEGER," +
                "contact_id INTEGER," +
                "FOREIGN KEY(utilisateur_id) REFERENCES Utilisateur(id)," +
                "FOREIGN KEY(localisation_id) REFERENCES Localisation(id)," +
                "FOREIGN KEY(contact_id) REFERENCES ContactUrgence(id))");

        db.execSQL("CREATE TABLE ListeVerification (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "type TEXT," +
                "texte TEXT," +
                "estEffectue INTEGER," +
                "utilisateur_id INTEGER," +
                "FOREIGN KEY(utilisateur_id) REFERENCES Utilisateur(id))");


        db.execSQL("CREATE TABLE ContactUrgence (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "nom TEXT," +
                "telephone TEXT," +
                "utilisateur_id INTEGER," +
                "FOREIGN KEY(utilisateur_id) REFERENCES Utilisateur(id))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // si tu modifies la DB plus tard
        db.execSQL("DROP TABLE IF EXISTS ContactUrgence");
        db.execSQL("DROP TABLE IF EXISTS ListeVerification");
        db.execSQL("DROP TABLE IF EXISTS AlerteUrgence");
        db.execSQL("DROP TABLE IF EXISTS Localisation");
        db.execSQL("DROP TABLE IF EXISTS Utilisateur");
        onCreate(db);
    }
}