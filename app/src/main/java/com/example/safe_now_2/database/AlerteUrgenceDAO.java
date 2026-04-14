package com.example.safe_now_2.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.format.DateFormat;

import java.util.Date;

public class AlerteUrgenceDAO {

    private SQLiteDatabase db;

    public AlerteUrgenceDAO(Context context) {
        db = new DatabaseHelper(context).getWritableDatabase();
    }

    // ===================== INSERT GÉNÉRAL =====================
    public long insert(String dateIso, String statut, int utilisateurId, int localisationId) {
        ContentValues values = new ContentValues();
        values.put("dateIso", dateIso);
        values.put("statut", statut);
        values.put("utilisateur_id", utilisateurId);
        values.put("localisation_id", localisationId);
        return db.insert("AlerteUrgence", null, values);
    }

    // ✅ NOUVEAU : Insert Appel d'urgence
    public long insertAppelUrgence(String numero, String type, int utilisateurId, String localisation) {
        String dateIso = getCurrentDateIso();
        ContentValues values = new ContentValues();
        values.put("dateIso", dateIso);
        values.put("type_action", "APPEL");
        values.put("numero_destination", numero);
        values.put("statut", "EN_COURS");
        values.put("utilisateur_id", utilisateurId);
        values.put("localisation", localisation);
        return db.insert("AlerteUrgence", null, values);
    }

    // ✅ NOUVEAU : Insert SMS SOS
    public long insertSmsUrgence(String destinataireNom, String destinataireTel, int utilisateurId, String localisation, String message) {
        String dateIso = getCurrentDateIso();
        ContentValues values = new ContentValues();
        values.put("dateIso", dateIso);
        values.put("type_action", "SMS");
        values.put("destinataire_nom", destinataireNom);
        values.put("numero_destination", destinataireTel);
        values.put("statut", "ENVOYE");
        values.put("utilisateur_id", utilisateurId);
        values.put("localisation", localisation);
        values.put("message_contenu", message);
        return db.insert("AlerteUrgence", null, values);
    }

    // ===================== GET ALL =====================
    public Cursor getAll() {
        return db.rawQuery("SELECT * FROM AlerteUrgence ORDER BY dateIso DESC", null);
    }

    public Cursor getByUtilisateur(int utilisateurId) {
        return db.rawQuery("SELECT * FROM AlerteUrgence WHERE utilisateur_id=? ORDER BY dateIso DESC",
                new String[]{String.valueOf(utilisateurId)});
    }

    // ✅ NOUVEAU : Get derniers appels/SMS
    public Cursor getDerniersAppelsSms(int utilisateurId, int limit) {
        return db.rawQuery("SELECT * FROM AlerteUrgence WHERE utilisateur_id=? AND (type_action='APPEL' OR type_action='SMS') ORDER BY dateIso DESC LIMIT ?",
                new String[]{String.valueOf(utilisateurId), String.valueOf(limit)});
    }

    // ✅ NOUVEAU : Get stats par type
    public Cursor getStatsByType(int utilisateurId) {
        return db.rawQuery("SELECT type_action, COUNT(*) as count FROM AlerteUrgence WHERE utilisateur_id=? GROUP BY type_action",
                new String[]{String.valueOf(utilisateurId)});
    }

    // ===================== UPDATE =====================
    public void updateStatut(int id, String statut) {
        ContentValues values = new ContentValues();
        values.put("statut", statut);
        db.update("AlerteUrgence", values, "id=?", new String[]{String.valueOf(id)});
    }

    // ✅ NOUVEAU : Update appel terminé
    public void updateAppelTermine(int id, String duree) {
        ContentValues values = new ContentValues();
        values.put("statut", "TERMINE");
        values.put("duree_appel", duree);
        db.update("AlerteUrgence", values, "id=?", new String[]{String.valueOf(id)});
    }

    public void delete(int id) {
        db.delete("AlerteUrgence", "id=?", new String[]{String.valueOf(id)});
    }

    // ===================== UTILITAIRES =====================
    private String getCurrentDateIso() {
        long now = System.currentTimeMillis();
        return DateFormat.format("yyyy-MM-dd HH:mm:ss", now).toString();
    }

    // Compter alertes du jour
    public int countAlerteToday(int utilisateurId) {
        String today = DateFormat.format("yyyy-MM-dd", System.currentTimeMillis()).toString();
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM AlerteUrgence WHERE utilisateur_id=? AND date(dateIso)=?",
                new String[]{String.valueOf(utilisateurId), today}
        );
        cursor.moveToFirst();
        int count = cursor.getInt(0);
        cursor.close();
        return count;
    }
}