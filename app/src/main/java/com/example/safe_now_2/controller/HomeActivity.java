package com.example.safe_now_2.controller;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.safe_now_2.CheckListActivity;
import com.example.safe_now_2.Contact_activity;
import com.example.safe_now_2.utils.PermissionDialogHelper;
import com.google.android.material.button.MaterialButton;
import com.example.safe_now_2.R;
import com.example.safe_now_2.model.UserSessionManager;
import com.example.safe_now_2.utils.PermissionHelper;

/**
 * ═══════════════════════════════════════════════════════════════
 * HomeActivity — CONTROLLER (MVC)
 *
 * Phase 2 des permissions : CALL_PHONE, SEND_SMS, READ_CONTACTS
 * demandées ICI (on-demand), pas au démarrage.
 * ═══════════════════════════════════════════════════════════════
 */
public class HomeActivity extends AppCompatActivity {

    // ── Vues ──────────────────────────────────────────────────────
//    private TextView       tvWelcome;
//    private MaterialButton btnSOS;
//    private MaterialButton btnLogout;
//
//    // ── Model ─────────────────────────────────────────────────────
//    private UserSessionManager sessionManager;
//
    // ── Launcher permissions Phase 2 ──────────────────────────────
    private ActivityResultLauncher<String[]> criticalPermLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        setupBottomNav();

//        // Init model
//        sessionManager = UserSessionManager.getInstance(this);
//
//        // Bind vues
//        tvWelcome = findViewById(R.id.tv_welcome);
//        btnSOS    = findViewById(R.id.btn_sos);
//        btnLogout = findViewById(R.id.btn_logout);
//
//        // Afficher le numéro sauvegardé (ou nom Firebase plus tard)
//        String phone = sessionManager.getUserPhone();
//        tvWelcome.setText("Bienvenue" + (phone.isEmpty() ? "" : " · " + phone));
//
        // Init launcher permissions critiques
        initCriticalPermissionsLauncher();

        // Dès l'affichage du Home, demander les permissions d'action
        requestCriticalPermissionsIfNeeded();
//
//        // Bouton SOS principal
//        btnSOS.setOnClickListener(v -> triggerSOS());
//
//        // Déconnexion
//        btnLogout.setOnClickListener(v -> logout());
    }
//
//    // ═══════════════════════════════════════════════════════════════
//    // PERMISSIONS PHASE 2 (on-demand)
//    // ═══════════════════════════════════════════════════════════════
//
private void initCriticalPermissionsLauncher() {
    criticalPermLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            results -> {

                boolean allGranted = !results.containsValue(false);

                if (allGranted) {
                    Toast.makeText(this,
                            "Permissions accordées ",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                //  Vérifier si "Ne plus demander" est activé
                boolean showRationale = false;
                String[] perms = PermissionHelper.getCriticalActionPermissions();

                for (String perm : perms) {
                    if (shouldShowRequestPermissionRationale(perm)) {
                        showRationale = true;
                        break;
                    }
                }

                if (showRationale) {
                    // ─── Cas 1 : refus simple ───
                    PermissionDialogHelper.showExplanationDialog(
                            this,
                            "Ces permissions sont indispensables pour envoyer des alertes SOS et contacter les secours.",
                            () -> requestCriticalPermissionsIfNeeded()
                    );

                } else {
                    // ─── Cas 2 : "Ne plus demander" ───
                    PermissionDialogHelper.openSettingsDialog(
                            this,
                            "Les permissions sont bloquées. Activez-les pour permettre les appels d'urgence et l'envoi de SMS."
                    );
                }
            }
    );
}

    private void requestCriticalPermissionsIfNeeded() {
        String[] perms = PermissionHelper.getCriticalActionPermissions();

        if (PermissionHelper.allGranted(this, perms)) {
            return; // déjà OK
        }

        PermissionHelper.requestPermissions(criticalPermLauncher, perms);
    }


//    private void showPermissionExplanationDialog() {
//        new androidx.appcompat.app.AlertDialog.Builder(this)
//                .setTitle("Permissions nécessaires")
//                .setMessage("Ces permissions sont indispensables pour envoyer une alerte SOS et contacter les secours.")
//                .setPositiveButton("Réessayer", (dialog, which) -> {
//                    requestCriticalPermissionsIfNeeded();
//                })
//                .setNegativeButton("Annuler", null)
//                .show();
//    }
//
//
//    private void openAppSettings() {
//        new androidx.appcompat.app.AlertDialog.Builder(this)
//                .setTitle("Permissions bloquées")
//                .setMessage("Veuillez activer les permissions manuellement dans les paramètres pour utiliser le SOS.")
//                .setPositiveButton("Ouvrir paramètres", (dialog, which) -> {
//                    Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//                    Uri uri = Uri.fromParts("package", getPackageName(), null);
//                    intent.setData(uri);
//                    startActivity(intent);
//                })
//                .setNegativeButton("Annuler", null)
//                .show();
//    }
//
//    // ═══════════════════════════════════════════════════════════════
//    // ACTIONS
//    // ═══════════════════════════════════════════════════════════════
//
//    private void triggerSOS() {
//        // Vérifier permissions avant action critique
//        if (!PermissionHelper.allGranted(this,
//                PermissionHelper.getCriticalActionPermissions())) {
//            requestCriticalPermissionsIfNeeded();
//            return;
//        }
//        // ── Ici : logique SOS (appel, SMS, partage position) ──────
//        Toast.makeText(this, "🚨 SOS déclenché !", Toast.LENGTH_SHORT).show();
//    }
//
//    private void logout() {
//        sessionManager.logout();
//        // ─── [FIREBASE] logout() appelle déjà FirebaseAuth.signOut()
//        Intent intent = new Intent(this, SplashActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        startActivity(intent);
//        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
//    }
private void setupBottomNav() {

    findViewById(R.id.nav_home).setOnClickListener(v -> {

    });

    findViewById(R.id.nav_contacts).setOnClickListener(v -> {
        startActivity(new Intent(this, Contact_activity.class));
    });

    findViewById(R.id.nav_sos).setOnClickListener(v -> {

    });

    findViewById(R.id.nav_history).setOnClickListener(v -> {
        startActivity(new Intent(this, MainActivity.class));
    });

    findViewById(R.id.nav_checklist).setOnClickListener(v -> {
        startActivity(new Intent(this, CheckListActivity.class));
    });
}
}
