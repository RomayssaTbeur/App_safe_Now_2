package com.example.safe_now_2.utils;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

public class PermissionDialogHelper {

    public static void showExplanationDialog(
            Activity activity,
            String message,
            Runnable onRetry
    ) {
        new androidx.appcompat.app.AlertDialog.Builder(activity)
                .setTitle("Permissions nécessaires")
                .setMessage(message)
                .setPositiveButton("Réessayer", (dialog, which) -> {
                    if (onRetry != null) onRetry.run();
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    public static void openSettingsDialog(Activity activity, String message) {
        new androidx.appcompat.app.AlertDialog.Builder(activity)
                .setTitle("Permissions bloquées")
                .setMessage(message)
                .setPositiveButton("Ouvrir paramètres", (dialog, which) -> {
                    Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
                    intent.setData(uri);
                    activity.startActivity(intent);
                })
                .setNegativeButton("Annuler", null)
                .show();
    }
}
