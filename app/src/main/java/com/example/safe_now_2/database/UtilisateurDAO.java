package com.example.safe_now_2.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class UtilisateurDAO {

    private SQLiteDatabase db;

    public UtilisateurDAO(Context context) {
        db = new DatabaseHelper(context).getWritableDatabase();
    }

    public long insert(String nom, String telephone) {
        ContentValues values = new ContentValues();
        values.put("nom", nom);
        values.put("telephone", telephone);
        return db.insert("Utilisateur", null, values);
    }

    public Cursor getAll() {
        return db.rawQuery("SELECT * FROM Utilisateur", null);
    }

    public Cursor getById(int id) {
        return db.rawQuery("SELECT * FROM Utilisateur WHERE id=?",
                new String[]{String.valueOf(id)});
    }

    public void update(int id, String nom, String telephone) {
        ContentValues values = new ContentValues();
        values.put("nom", nom);
        values.put("telephone", telephone);
        db.update("Utilisateur", values, "id=?",
                new String[]{String.valueOf(id)});
    }

    public void delete(int id) {
        db.delete("Utilisateur", "id=?",
                new String[]{String.valueOf(id)});
    }
}