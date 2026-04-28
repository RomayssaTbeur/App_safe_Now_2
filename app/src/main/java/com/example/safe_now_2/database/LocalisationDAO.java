package com.example.safe_now_2.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class LocalisationDAO {

    private SQLiteDatabase db;

    public LocalisationDAO(Context context) {
        db = new DatabaseHelper(context).getWritableDatabase();
    }

    public long insert(double latitude, double longitude) {
        ContentValues values = new ContentValues();
        values.put("latitude", latitude);
        values.put("longitude", longitude);
        return db.insert("Localisation", null, values);
    }

    public Cursor getById(int id) {
        return db.rawQuery("SELECT * FROM Localisation WHERE id=?",
                new String[]{String.valueOf(id)});
    }

    public void delete(int id) {
        db.delete("Localisation", "id=?",
                new String[]{String.valueOf(id)});
    }
}