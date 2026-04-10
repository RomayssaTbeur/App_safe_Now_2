package com.example.safe_now_2;


import android.content.Context;
import android.content.SharedPreferences;

/**
 * Gestionnaire des préférences d'authentification.
 * Utilise SharedPreferences pour mémoriser l'état de connexion
 * entre les sessions (authentification unique).
 */
public class AuthPreferences {

    private static final String PREF_NAME        = "safenow_auth";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_PHONE_NUMBER = "phone_number";
    private static final String KEY_VERIFIED_AT  = "verified_at";

    private final SharedPreferences prefs;

    public AuthPreferences(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /** Retourne true si l'utilisateur s'est déjà authentifié avec succès. */
    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    /** Persiste l'état "authentifié" avec le numéro validé. */
    public void setLoggedIn(String phoneNumber) {
        prefs.edit()
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .putString(KEY_PHONE_NUMBER, phoneNumber)
                .putLong(KEY_VERIFIED_AT, System.currentTimeMillis())
                .apply();
    }

    /** Récupère le numéro de téléphone enregistré. */
    public String getPhoneNumber() {
        return prefs.getString(KEY_PHONE_NUMBER, "");
    }

    /** Supprime toutes les données (déconnexion/reset). */
    public void clear() {
        prefs.edit().clear().apply();
    }
}