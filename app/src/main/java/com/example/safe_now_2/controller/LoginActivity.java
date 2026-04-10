package com.example.safe_now_2.controller;


import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.example.safe_now_2.R;
import com.example.safe_now_2.model.UserSessionManager;

/**
 * ═══════════════════════════════════════════════════════════════
 * LoginActivity — CONTROLLER (MVC) — Version statique (simulation)
 *
 * ╔══════════════════════════════════════════════════════════════╗
 * ║  MIGRATION FIREBASE OTP (étape future) :                    ║
 * ║                                                             ║
 * ║  1. Ajouter PhoneAuthProvider dans build.gradle :           ║
 * ║     implementation 'com.google.firebase:firebase-auth'      ║
 * ║                                                             ║
 * ║  2. Remplacer btnLogin.setOnClickListener par :             ║
 * ║     → Étape A : saisir numéro → PhoneAuthProvider.          ║
 * ║       verifyPhoneNumber(options)                             ║
 * ║     → Étape B : saisir OTP reçu par SMS → vérifier avec    ║
 * ║       PhoneAuthCredential credential =                      ║
 * ║         PhoneAuthProvider.getCredential(verificationId, otp)║
 * ║       FirebaseAuth.getInstance()                            ║
 * ║         .signInWithCredential(credential)                   ║
 * ║         .addOnSuccessListener(r -> goToHome())              ║
 * ║                                                             ║
 * ║  3. Supprimer sessionManager.saveLoginState(true)           ║
 * ║     (Firebase gère le token automatiquement)                ║
 * ║                                                             ║
 * ║  4. Dans UserSessionManager.isLoggedIn() :                  ║
 * ║     return FirebaseAuth.getInstance()                       ║
 * ║              .getCurrentUser() != null;                     ║
 * ╚══════════════════════════════════════════════════════════════╝
 * ═══════════════════════════════════════════════════════════════
 */
public class LoginActivity extends AppCompatActivity {

    // ── Vues ──────────────────────────────────────────────────────
//    private EditText       etPhone;
    private MaterialButton btnLogin;

    // ── Model ─────────────────────────────────────────────────────
    private UserSessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Init model
//        sessionManager = UserSessionManager.getInstance(this);
//
//        // Bind vues
//        etPhone  = findViewById(R.id.et_phone);
        btnLogin = findViewById(R.id.btn_login);

        // ── [ACTUEL] Login simulé ──────────────────────────────────
        btnLogin.setOnClickListener(v -> performStaticLogin());

        // ─── [FIREBASE] Remplacer performStaticLogin() par :
        // ─── sendOtp() → afficher champ OTP → verifyOtp() → goToHome()
    }

    // ═══════════════════════════════════════════════════════════════
    // LOGIN STATIQUE (temporaire)
    // ═══════════════════════════════════════════════════════════════

    /**
     * Simule un login réussi.
     * VERSION FIREBASE : cette méthode est supprimée et remplacée
     * par le flow OTP (voir commentaire MIGRATION ci-dessus).
     */
    private void performStaticLogin() {
//        String phone = etPhone.getText().toString().trim();
//
//        // Validation basique (sera remplacée par vérification OTP Firebase)
//        if (TextUtils.isEmpty(phone)) {
//            etPhone.setError("Entrez votre numéro");
//            return;
//        }
//
//        // ── [ACTUEL] Sauvegarder session localement ────────────────
//        sessionManager.saveLoginState(true);
//        sessionManager.saveUserPhone(phone); // déjà utile pour l'UI post-login

        // ─── [FIREBASE] Ces 2 lignes sont supprimées :
        // ─── Firebase gère le token ; isLoggedIn() lira getCurrentUser()

        // Naviguer vers Home
        goToHome();
    }

    // ═══════════════════════════════════════════════════════════════
    // NAVIGATION
    // ═══════════════════════════════════════════════════════════════

    private void goToHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish(); // pas de retour vers Login
    }
}
