package com.example.safe_now_2.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class ContactUrgenceDAO {

    private SQLiteDatabase db;

    public ContactUrgenceDAO(Context context) {
        db = new DatabaseHelper(context).getWritableDatabase();
    }

    public long insert(String nom, String telephone, int utilisateurId) {
        ContentValues values = new ContentValues();
        values.put("nom", nom);
        values.put("telephone", telephone);
        values.put("utilisateur_id", utilisateurId);
        return db.insert("ContactUrgence", null, values);
    }

    public Cursor getByUtilisateur(int utilisateurId) {
        return db.rawQuery("SELECT * FROM ContactUrgence WHERE utilisateur_id=?",
                new String[]{String.valueOf(utilisateurId)});
    }

    public Cursor getAll() {
        return db.rawQuery("SELECT * FROM ContactUrgence", null);
    }

    public Cursor getByAlerte(int alerteId) {
        return db.rawQuery("SELECT * FROM ContactUrgence WHERE alerte_id=?",
                new String[]{String.valueOf(alerteId)});
    }

    public void delete(int id) {
        db.delete("ContactUrgence", "id=?",
                new String[]{String.valueOf(id)});
    }
}