package com.example.safe_now_2.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class AlerteUrgenceDAO {

    private SQLiteDatabase db;

    public AlerteUrgenceDAO(Context context) {
        db = new DatabaseHelper(context).getWritableDatabase();
    }

    public long insert(String dateIso, String statut, int utilisateurId, int localisationId) {
        ContentValues values = new ContentValues();
        values.put("dateIso", dateIso);
        values.put("statut", statut);
        values.put("utilisateur_id", utilisateurId);
        values.put("localisation_id", localisationId);
        return db.insert("AlerteUrgence", null, values);
    }

    public Cursor getAll() {
        return db.rawQuery("SELECT * FROM AlerteUrgence", null);
    }

    public Cursor getByUtilisateur(int utilisateurId) {
        return db.rawQuery("SELECT * FROM AlerteUrgence WHERE utilisateur_id=?",
                new String[]{String.valueOf(utilisateurId)});
    }

    public void updateStatut(int id, String statut) {
        ContentValues values = new ContentValues();
        values.put("statut", statut);
        db.update("AlerteUrgence", values, "id=?",
                new String[]{String.valueOf(id)});
    }

    public void delete(int id) {
        db.delete("AlerteUrgence", "id=?",
                new String[]{String.valueOf(id)});
    }
}