package com.example.safe_now_2.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class ListeVerificationDAO {

    private SQLiteDatabase db;

    public ListeVerificationDAO(Context context) {
        db = new DatabaseHelper(context).getWritableDatabase();
    }

    public long insert(String type, String texte, boolean estEffectue, int utilisateurId) {
        ContentValues values = new ContentValues();
        values.put("type", type);
        values.put("texte", texte);
        values.put("estEffectue", estEffectue ? 1 : 0);
        values.put("utilisateur_id", utilisateurId);
        return db.insert("ListeVerification", null, values);
    }

    public Cursor getByUtilisateur(int utilisateurId) {
        return db.rawQuery("SELECT * FROM ListeVerification WHERE utilisateur_id=?",
                new String[]{String.valueOf(utilisateurId)});
    }

    public void marquerEffectue(int id) {
        ContentValues values = new ContentValues();
        values.put("estEffectue", 1);
        db.update("ListeVerification", values, "id=?",
                new String[]{String.valueOf(id)});
    }

    public void delete(int id) {
        db.delete("ListeVerification", "id=?",
                new String[]{String.valueOf(id)});
    }
}