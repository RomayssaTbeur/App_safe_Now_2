package com.example.safe_now_2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;

/**
 * Écran principal de SafeNow.
 *
 * Accessible uniquement après authentification réussie.
 * Si l'état auth est absent (ex: données effacées), renvoie
 * automatiquement vers LoginActivity.
 *
 * Note : cet écran est un placeholder.
 *        Remplacez son contenu par vos fonctionnalités d'urgence.
 */
public class MainActivity extends AppCompatActivity {

    private AuthPreferences authPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        authPrefs = new AuthPreferences(this);

        if (!authPrefs.isLoggedIn()) {
            goToLogin();
            return;
        }

        setContentView(R.layout.activity_main);

        getWindow().setStatusBarColor(
                ContextCompat.getColor(this, R.color.surface));
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        initViews();

        // ── Gestion du bouton “back” avec OnBackPressedDispatcher ───────────────
        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Minimiser l'app au lieu de fermer
                moveTaskToBack(true);
            }
        });
    }

    // ── Initialisation ────────────────────────────────────────────────────────

    private void initViews() {
        // Afficher le numéro enregistré
        TextView tvWelcome = findViewById(R.id.tv_welcome);
        String phone = authPrefs.getPhoneNumber();
        if (tvWelcome != null && !phone.isEmpty()) {
            tvWelcome.setText("Connecté avec " + phone);
        }

        // Bouton SOS
        MaterialButton btnEmergency = findViewById(R.id.btn_emergency);
        if (btnEmergency != null) {
            btnEmergency.setOnClickListener(v -> onSosPressed());
        }
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    /**
     * Déclenché lors de l'appui sur le bouton SOS.
     * Implémentez ici votre logique d'alerte d'urgence.
     */
    private void onSosPressed() {
        // TODO: implémenter l'envoi d'alerte, géolocalisation, etc.
        Toast.makeText(this, "🚨 Alerte d'urgence envoyée !", Toast.LENGTH_LONG).show();
    }

    /**
     * Déconnexion / Reset de l'auth.
     * Appelez cette méthode depuis un menu Paramètres si nécessaire.
     */
    public void logout() {
        authPrefs.clear();
        OtpManager.getInstance().invalidate();
        goToLogin();
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    private void goToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}
