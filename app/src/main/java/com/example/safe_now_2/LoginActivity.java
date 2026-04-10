package com.example.safe_now_2;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;

/**
 * Écran de saisie du numéro de téléphone.
 *
 * Flux :
 *  1. L'activité vérifie d'abord si l'utilisateur est déjà authentifié
 *     (SharedPreferences) → redirige directement vers MainActivity.
 *  2. Sinon, l'utilisateur saisit son numéro et appuie sur "Send Code".
 *  3. Un OTP est généré et on navigue vers OtpVerificationActivity.
 */
public class LoginActivity extends AppCompatActivity {

    // ── Views ─────────────────────────────────────────────────────────────────
    private EditText etPhone;
    private TextView tvPhoneError;
    private MaterialButton btnSendCode;

    // ── Helpers ───────────────────────────────────────────────────────────────
    private AuthPreferences authPrefs;
    private OtpManager otpManager;

    // ── Constants ─────────────────────────────────────────────────────────────
    private static final String COUNTRY_CODE = "+212";

    // ─────────────────────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialiser les préférences AVANT setContentView
        authPrefs = new AuthPreferences(this);

        // ┌─ VÉRIFICATION AUTH RAPIDE ──────────────────────────────────────────
        // Si l'utilisateur a déjà validé son numéro → accès direct à l'appli.
        // Aucun écran de login affiché = zéro friction pour une app d'urgence.
        if (authPrefs.isLoggedIn()) {
            goToMain();
            return; // Ne pas continuer onCreate
        }
        // └────────────────────────────────────────────────────────────────────

        setContentView(R.layout.activity_login);

        // Thème sombre forcé pour cet écran
        getWindow().setStatusBarColor(
                ContextCompat.getColor(this, R.color.dark_background));
        getWindow().getDecorView().setSystemUiVisibility(0); // icônes claires

        otpManager = OtpManager.getInstance();

        initViews();
        setupTermsText();
        setupListeners();
        animateEntrance();
    }

    // ── Initialisation ────────────────────────────────────────────────────────

    private void initViews() {
        etPhone      = findViewById(R.id.et_phone);
        tvPhoneError = findViewById(R.id.tv_phone_error);
        btnSendCode  = findViewById(R.id.btn_send_code);

        // Bouton retour
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }

    /**
     * Construit le texte "Terms of Service and Privacy Policy" avec liens cliquables.
     */
    private void setupTermsText() {
        TextView tvTerms = findViewById(R.id.tv_terms);
        if (tvTerms == null) return;

        String full = "By continuing, you agree to SafeNow's Terms of Service and Privacy Policy.";
        SpannableString ss = new SpannableString(full);

        int tosStart = full.indexOf("Terms of Service");
        int tosEnd   = tosStart + "Terms of Service".length();
        int ppStart  = full.indexOf("Privacy Policy");
        int ppEnd    = ppStart + "Privacy Policy".length();

        int white = ContextCompat.getColor(this, android.R.color.white);

        ss.setSpan(buildClickableLink(() ->
                        Toast.makeText(this, "Terms of Service", Toast.LENGTH_SHORT).show()),
                tosStart, tosEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss.setSpan(new ForegroundColorSpan(white),
                tosStart, tosEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        ss.setSpan(buildClickableLink(() ->
                        Toast.makeText(this, "Privacy Policy", Toast.LENGTH_SHORT).show()),
                ppStart, ppEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss.setSpan(new ForegroundColorSpan(white),
                ppStart, ppEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        tvTerms.setText(ss);
        tvTerms.setMovementMethod(LinkMovementMethod.getInstance());
        tvTerms.setHighlightColor(android.graphics.Color.TRANSPARENT);
    }

    private ClickableSpan buildClickableLink(Runnable action) {
        return new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) { action.run(); }
            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(true);
            }
        };
    }

    // ── Listeners ─────────────────────────────────────────────────────────────

    private void setupListeners() {
        btnSendCode.setOnClickListener(v -> {
            String phone = etPhone.getText().toString().trim();
            if (validatePhone(phone)) {
                sendCode(phone);
            }
        });

        // Effacer l'erreur dès que l'utilisateur retape
        etPhone.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                hideError();
            }
        });
    }

    // ── Validation ────────────────────────────────────────────────────────────

    /**
     * Valide le numéro marocain : 9 chiffres commençant par 6 ou 7.
     * Adaptez selon votre marché.
     */
    private boolean validatePhone(String phone) {
        if (phone.isEmpty()) {
            showError(getString(R.string.error_invalid_phone));
            return false;
        }
        if (!phone.matches("[67]\\d{8}")) {
            showError(getString(R.string.error_invalid_phone));
            return false;
        }
        return true;
    }

    private void showError(String msg) {
        tvPhoneError.setText(msg);
        tvPhoneError.setVisibility(View.VISIBLE);
    }

    private void hideError() {
        tvPhoneError.setVisibility(View.GONE);
    }

    // ── Envoi OTP ─────────────────────────────────────────────────────────────
    /**private void sendCode(String localPhone) {
        String fullPhone;
        // Si on est sur émulateur, utiliser le numéro de test
        if (android.os.Build.FINGERPRINT.contains("generic")) {
            fullPhone = "+212600000001"; // numéro de test Firebase
        } else {
            fullPhone = COUNTRY_CODE + localPhone; // numéro réel
        }

        // Désactiver le bouton pendant l'envoi
        btnSendCode.setEnabled(false);
        btnSendCode.setText("Sending…");

        otpManager.sendOtp(fullPhone, this, new OtpManager.SendCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    btnSendCode.setEnabled(true);
                    btnSendCode.setText(getString(R.string.btn_send_code));
                    goToOtp(fullPhone);
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    btnSendCode.setEnabled(true);
                    btnSendCode.setText(getString(R.string.btn_send_code));
                    showError(message);
                });
            }
        });
    } **/
    private void sendCode(String localPhone) {
        String fullPhone = COUNTRY_CODE + localPhone;

        // Désactiver le bouton pendant l'envoi
        btnSendCode.setEnabled(false);
        btnSendCode.setText("Sending…");

        otpManager.sendOtp(fullPhone, this, new OtpManager.SendCallback() {

            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    btnSendCode.setEnabled(true);
                    btnSendCode.setText(getString(R.string.btn_send_code));

                    goToOtp(fullPhone);
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    btnSendCode.setEnabled(true);
                    btnSendCode.setText(getString(R.string.btn_send_code));
                    showError(message);
                });
            }
        }); // ✅ <-- N'oublie pas ce point-virgule et cette accolade

    } // ✅ <-- fermeture correcte de sendCode()

// ── Navigation ────────────────────────────────────────────────────────────

    /** Navigue vers l'écran de vérification OTP. */
    private void goToOtp(String fullPhone) {
        Intent intent = new Intent(this, OtpVerificationActivity.class);
        intent.putExtra(OtpVerificationActivity.EXTRA_PHONE, fullPhone);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
    /**
     * Navigue vers MainActivity et ferme la pile d'authentification.
     * L'utilisateur ne peut plus revenir à l'écran de login via "Back".
     */
    private void goToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    // ── Animation d'entrée ────────────────────────────────────────────────────

    private void animateEntrance() {
        View contentRoot = findViewById(android.R.id.content);
        AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
        fadeIn.setDuration(500);
        fadeIn.setFillAfter(true);
        contentRoot.startAnimation(fadeIn);
    }

    // ── TextWatcher simplifié ─────────────────────────────────────────────────

    /** Implémentation minimaliste de TextWatcher pour éviter le boilerplate. */
    private abstract static class SimpleTextWatcher
            implements android.text.TextWatcher {
        @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
        @Override public void afterTextChanged(android.text.Editable s) {}
    }
}