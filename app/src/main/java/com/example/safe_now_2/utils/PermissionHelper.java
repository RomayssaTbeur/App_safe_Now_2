package com.example.safe_now_2.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.activity.result.ActivityResultLauncher;
import androidx.core.content.ContextCompat;

/**
 * PermissionHelper — Gestion centralisée des permissions (on-demand).
 *
 * Principe UX d'urgence :
 *  • Phase 1 (Splash→Login) : LOCATION + NOTIFICATIONS
 *  • Phase 2 (Home affiché) : CALL_PHONE + SEND_SMS + READ_CONTACTS
 *
 * Utilise ActivityResultLauncher (API moderne, pas requestPermissions déprécié).
 */
public class PermissionHelper {

    // ── Permissions Phase 1 : essentielles au démarrage ──────────
    public static String[] getEssentialPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ : POST_NOTIFICATIONS nécessite permission explicite
            return new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.POST_NOTIFICATIONS
            };
        } else {
            return new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION
            };
        }
    }

    // ── Permissions Phase 2 : demandées depuis HomeActivity ──────
    public static String[] getCriticalActionPermissions() {
        return new String[]{
                Manifest.permission.CALL_PHONE,
                Manifest.permission.SEND_SMS,
//                Manifest.permission.READ_CONTACTS
        };
    }

    /**
     * Vérifie si TOUTES les permissions du tableau sont accordées.
     */
    public static boolean allGranted(Activity activity, String[] permissions) {
        for (String perm : permissions) {
            if (ContextCompat.checkSelfPermission(activity, perm)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * Lance la demande de permissions via ActivityResultLauncher.
     * Le launcher est créé dans l'Activity (voir SplashActivity).
     */
    public static void requestPermissions(
            ActivityResultLauncher<String[]> launcher,
            String[] permissions) {
        launcher.launch(permissions);
    }
}
