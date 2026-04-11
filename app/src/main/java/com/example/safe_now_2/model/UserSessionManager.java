package com.example.safe_now_2.model;


import android.content.Context;
import android.content.SharedPreferences;

/**
 * ═══════════════════════════════════════════════════════════════
 * UserSessionManager — MODEL (MVC)
 * Gère la session utilisateur via SharedPreferences.
 *
 * ╔══════════════════════════════════════════════════════════════╗
 * ║  MIGRATION FIREBASE (étape future) :                        ║
 * ║  Remplacer le corps de isLoggedIn() par :                   ║
 * ║    return FirebaseAuth.getInstance().getCurrentUser() != null║
 * ║  Remplacer saveLoginState(true) par l'appel post-OTP :      ║
 * ║    FirebaseAuth.getInstance().signInWithCredential(...)      ║
 * ║  Remplacer logout() par :                                    ║
 * ║    FirebaseAuth.getInstance().signOut()                      ║
 * ╚══════════════════════════════════════════════════════════════╝
 * ═══════════════════════════════════════════════════════════════
 */
public class UserSessionManager {

    // ── Constantes SharedPreferences ──────────────────────────────
    private static final String PREF_NAME       = "SafeNowSession";
    private static final String KEY_IS_LOGGED   = "is_logged_in";
    private static final String KEY_USER_PHONE  = "user_phone";    // utile pour OTP plus tard

    // ── Instance Singleton ────────────────────────────────────────
    private static UserSessionManager instance;

    private final SharedPreferences prefs;

    // ── Constructeur privé (Singleton) ────────────────────────────
    private UserSessionManager(Context context) {
        // applicationContext évite les fuites mémoire
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Retourne l'instance unique (thread-safe basique).
     * Appelée depuis n'importe quelle Activity : UserSessionManager.getInstance(this)
     */
    public static UserSessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new UserSessionManager(context);
        }
        return instance;
    }

    // ═══════════════════════════════════════════════════════════════
    // MÉTHODE CLÉE — À remplacer par Firebase lors de la migration
    // ═══════════════════════════════════════════════════════════════

    /**
     * Vérifie si l'utilisateur est connecté.
     *
     * VERSION ACTUELLE  : lit SharedPreferences
     * VERSION FIREBASE  : return FirebaseAuth.getInstance().getCurrentUser() != null;
     */
    public boolean isLoggedIn() {
        // ─── [FIREBASE] Remplacer ces 2 lignes par :
        // ─── return FirebaseAuth.getInstance().getCurrentUser() != null;
       return prefs.getBoolean(KEY_IS_LOGGED, true);
    }

    /**
     * Sauvegarde l'état de connexion après login réussi.
     *
     * VERSION ACTUELLE  : écrit dans SharedPreferences
     * VERSION FIREBASE  : cette méthode devient inutile —
     *                     Firebase gère le token en interne.
     *                     Vous pouvez la garder pour stocker
     *                     des métadonnées locales (ex: userPhone).
     */
    public void saveLoginState(boolean isLoggedIn) {
        prefs.edit().putBoolean(KEY_IS_LOGGED, isLoggedIn).apply();
    }

    /**
     * Sauvegarde le numéro de téléphone (déjà utile pour OTP).
     * VERSION FIREBASE : utile pour pré-remplir l'UI post-auth.
     */
    public void saveUserPhone(String phone) {
        prefs.edit().putString(KEY_USER_PHONE, phone).apply();
    }

    public String getUserPhone() {
        return prefs.getString(KEY_USER_PHONE, "");
    }

    /**
     * Déconnexion.
     * VERSION FIREBASE : ajouter FirebaseAuth.getInstance().signOut();
     */
    public void logout() {
        prefs.edit()
                .putBoolean(KEY_IS_LOGGED, false)
                .remove(KEY_USER_PHONE)
                .apply();
        // ─── [FIREBASE] Ajouter : FirebaseAuth.getInstance().signOut();
    }
}