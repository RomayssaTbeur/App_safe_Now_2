package com.example.safe_now_2.controller;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.safe_now_2.utils.PermissionDialogHelper;
import com.google.android.material.button.MaterialButton;
import com.example.safe_now_2.R;
import com.example.safe_now_2.model.AuthPreferences;
import com.example.safe_now_2.utils.PermissionHelper;

/**
 * ═══════════════════════════════════════════════════════════════
 * SplashActivity — CONTROLLER (MVC)
 *
 * Logique :
 *  • isLoggedIn() == true  → animation fade + redirect HomeActivity ≤ 2s
 *  • isLoggedIn() == false → afficher btn_start, demander permissions,
 *                            redirect LoginActivity
 * ═══════════════════════════════════════════════════════════════
 */
public class SplashActivity extends AppCompatActivity {

    // ── Délai splash pour utilisateur connecté (ms) ───────────────
    private static final int SPLASH_DELAY_LOGGED_IN = 1500; // 1.5s ≤ 2s ✓

    // ── Durée transition fade (ms) ────────────────────────────────
    private static final int FADE_DURATION = 300;

    // ── Références vues ───────────────────────────────────────────
    private MaterialButton btnStart;
    private View  logoContainer;

    // ── Model ─────────────────────────────────────────────────────

    // ── ActivityResultLauncher (API moderne permissions) ──────────
    private ActivityResultLauncher<String[]> permissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_activity);

        // ── 1. Init Model ──────────────────────────────────────────
        AuthPreferences authPrefs = new AuthPreferences(this);

        // ── 2. Bind vues ───────────────────────────────────────────
        btnStart      = findViewById(R.id.btn_start);
        logoContainer = findViewById(R.id.logo_container);

        // ── 3. Init launcher permissions (AVANT de l'utiliser) ────
        initPermissionLauncher();

        // ── 4. Décision de routing ─────────────────────────────────
        if (authPrefs.isLoggedIn()) {
            handleLoggedInUser();
        } else {
            handleNewUser();
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // CAS 1 : Utilisateur déjà connecté
    // ═══════════════════════════════════════════════════════════════

    private void handleLoggedInUser() {
        // Cacher le bouton Start — l'utilisateur ne doit pas le voir
        btnStart.setVisibility(View.GONE);

        // Lancer l'animation logo (scale + fade-in léger)
        playLogoAnimation();

        // Redirection non bloquante vers Home après SPLASH_DELAY ms
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            navigateTo(HomeActivity.class);
        }, SPLASH_DELAY_LOGGED_IN);
    }

    // ═══════════════════════════════════════════════════════════════
    // CAS 2 : Nouvel utilisateur
    // ═══════════════════════════════════════════════════════════════

    private void handleNewUser() {
        // Afficher le bouton Start
        btnStart.setVisibility(View.VISIBLE);

        // Animation d'entrée du logo
        playLogoAnimation();

        // Clic sur Start → demander permissions essentielles
        btnStart.setOnClickListener(v -> requestEssentialPermissions());
    }

    // ═══════════════════════════════════════════════════════════════
    // GESTION PERMISSIONS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Initialise le launcher AVANT tout appel à launch().
     * ActivityResultLauncher doit être enregistré dans onCreate().
     */
    private void initPermissionLauncher() {
        permissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                results -> {
                    boolean allGranted = !results.containsValue(false);

                    if (allGranted) {
                        navigateTo(LoginActivity.class);
                        return;
                    }

                    // 🔥 analyser refus
                    boolean showRationale = false;
                    String[] perms = PermissionHelper.getEssentialPermissions();

                    for (String perm : perms) {
                        if (shouldShowRequestPermissionRationale(perm)) {
                            showRationale = true;
                            break;
                        }
                    }

                    if (showRationale) {
                        PermissionDialogHelper.showExplanationDialog(
                                this,
                                "Ces permissions sont nécessaires pour accéder à l'application et sécuriser votre compte.",
                                () -> requestEssentialPermissions()
                        );

                    } else {
                        PermissionDialogHelper.openSettingsDialog(
                                this,
                                "Activez les permissions dans les paramètres pour continuer l'utilisation de l'application."
                        );
                    }
                }
        );
    }

    private void requestEssentialPermissions() {
        String[] perms = PermissionHelper.getEssentialPermissions();

        if (PermissionHelper.allGranted(this, perms)) {
            navigateTo(LoginActivity.class);
            return;
        }

        PermissionHelper.requestPermissions(permissionLauncher, perms);
    }

//    private void showExplanationDialog() {
//        new androidx.appcompat.app.AlertDialog.Builder(this)
//                .setTitle("Permissions nécessaires")
//                .setMessage("Ces permissions sont nécessaires pour permettre les fonctionnalités d’urgence.")
//                .setPositiveButton("Réessayer", (dialog, which) -> {
//                    requestEssentialPermissions();
//                })
//                .setNegativeButton("Annuler", null)
//                .show();
//    }
//
//    private void openSettingsDialog() {
//        new androidx.appcompat.app.AlertDialog.Builder(this)
//                .setTitle("Permissions bloquées")
//                .setMessage("Veuillez activer les permissions manuellement dans les paramètres pour continuer.")
//                .setPositiveButton("Ouvrir paramètres", (dialog, which) -> {
//                    Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//                    Uri uri = Uri.fromParts("package", getPackageName(), null);
//                    intent.setData(uri);
//                    startActivity(intent);
//                })
//                .setNegativeButton("Annuler", null)
//                .show();
//    }

    // ═══════════════════════════════════════════════════════════════
    // ANIMATIONS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Animation légère : scale 0.85→1.0 + fade-in simultanés.
     * Durée : 300ms pour ne pas bloquer l'UX.
     */
    private void playLogoAnimation() {
        // Scale : légère montée en taille
        ScaleAnimation scale = new ScaleAnimation(
                0.85f, 1.0f,    // scaleX start→end
                0.85f, 1.0f,    // scaleY start→end
                Animation.RELATIVE_TO_SELF, 0.5f,   // pivot X centre
                Animation.RELATIVE_TO_SELF, 0.5f    // pivot Y centre
        );

        // Alpha : fondu entrant
        AlphaAnimation fade = new AlphaAnimation(0f, 1f);

        // Combiner les deux
        AnimationSet set = new AnimationSet(true);
        set.addAnimation(scale);
        set.addAnimation(fade);
        set.setDuration(FADE_DURATION);
        set.setFillAfter(true); // garder état final

        logoContainer.startAnimation(set);
    }

    // ═══════════════════════════════════════════════════════════════
    // NAVIGATION
    // ═══════════════════════════════════════════════════════════════

    /**
     * Navigation générique avec transition fade et finish().
     * finish() empêche le retour arrière vers le Splash.
     */
    private void navigateTo(Class<?> destination) {
        Intent intent = new Intent(this, destination);
        startActivity(intent);

        // Transition fade sortante (≤ 300ms)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

        // Détruire le Splash : pas de retour possible (app d'urgence = UX directe)
        finish();
    }
}
