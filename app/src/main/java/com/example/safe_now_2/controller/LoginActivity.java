package com.example.safe_now_2.controller;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.safe_now_2.R;
import com.example.safe_now_2.utils.OtpManager;
import com.google.android.material.button.MaterialButton;
import com.hbb20.CountryCodePicker;

public class LoginActivity extends AppCompatActivity {

    // Views
    private EditText etPhone;
    private TextView tvPhoneError;
    private MaterialButton btnSendCode;
    private CountryCodePicker ccp;

    private OtpManager otpManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        // Status bar
        getWindow().setStatusBarColor(
                ContextCompat.getColor(this, R.color.dark_background));
        getWindow().getDecorView().setSystemUiVisibility(0);

        otpManager = OtpManager.getInstance();

        initViews();
        setupCountryPicker();
        setupTermsText();
        setupListeners();
        animateEntrance();
    }

    // ---------------- INIT ----------------

    private void initViews() {
        etPhone = findViewById(R.id.et_phone);
        tvPhoneError = findViewById(R.id.tv_phone_error);
        btnSendCode = findViewById(R.id.btn_send_code);
        ccp = findViewById(R.id.ccp);

        ccp.setDialogTextColor(ContextCompat.getColor(this, R.color.login_text_primary));
        ccp.setDialogBackgroundColor(ContextCompat.getColor(this, R.color.login_background));

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
    }

    // ---------------- COUNTRY PICKER (FIX IMPORTANT) ----------------

    private void setupCountryPicker() {

        // Maroc par défaut
        ccp.setDefaultCountryUsingNameCode("MA");
        ccp.resetToDefaultCountry();

        // LIEN CRUCIAL (sinon validation cassée)
        ccp.registerCarrierNumberEditText(etPhone);

        // style optionnel
        ccp.setContentColor(
                ContextCompat.getColor(this, R.color.login_text_primary)
        );
    }

    // ---------------- TERMS ----------------

    private void setupTermsText() {
        TextView tvTerms = findViewById(R.id.tv_terms);

        String full = "By continuing, you agree to Terms of Service and Privacy Policy.";
        SpannableString ss = new SpannableString(full);

        int tosStart = full.indexOf("Terms of Service");
        int tosEnd = tosStart + "Terms of Service".length();

        int ppStart = full.indexOf("Privacy Policy");
        int ppEnd = ppStart + "Privacy Policy".length();

        // 🔵 couleur BLEUE (fixe)
        int linkColor = ContextCompat.getColor(this, R.color.link_color);

        ss.setSpan(createLink(() ->
                        Toast.makeText(this, "Terms clicked", Toast.LENGTH_SHORT).show()),
                tosStart, tosEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        ss.setSpan(new ForegroundColorSpan(linkColor),
                tosStart, tosEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        ss.setSpan(createLink(() ->
                        Toast.makeText(this, "Privacy clicked", Toast.LENGTH_SHORT).show()),
                ppStart, ppEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        ss.setSpan(new ForegroundColorSpan(linkColor),
                ppStart, ppEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        tvTerms.setText(ss);
        tvTerms.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private ClickableSpan createLink(Runnable action) {
        return new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                action.run();
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                ds.setUnderlineText(true);
            }
        };
    }

    // ---------------- LISTENERS ----------------

    private void setupListeners() {

        btnSendCode.setOnClickListener(v -> {
            if (validatePhone()) {
                sendCode();
            }
        });

        etPhone.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                hideError();
            }
        });
    }

    // ---------------- VALIDATION FIX ----------------

    private boolean validatePhone() {

        if (!ccp.isValidFullNumber()) {
            showError("Numéro invalide");
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

    // ---------------- OTP ----------------

    private void sendCode() {

        // 🔥 IMPORTANT: format Firebase correct
        String fullPhone = ccp.getFullNumberWithPlus();

        btnSendCode.setEnabled(false);
        btnSendCode.setText("Sending…");

        otpManager.sendOtp(fullPhone, this, new OtpManager.SendCallback() {

            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    btnSendCode.setEnabled(true);
                    btnSendCode.setText(R.string.btn_send_code);
                    goToOtp(fullPhone);
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    btnSendCode.setEnabled(true);
                    btnSendCode.setText(R.string.btn_send_code);
                    showError(message);
                });
            }
        });
    }

    // ---------------- NAVIGATION ----------------

    private void goToOtp(String fullPhone) {
        Intent intent = new Intent(this, OtpVerificationActivity.class);
        intent.putExtra(OtpVerificationActivity.EXTRA_PHONE, fullPhone);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    // ---------------- ANIMATION ----------------

    private void animateEntrance() {
        View root = findViewById(android.R.id.content);

        AlphaAnimation anim = new AlphaAnimation(0f, 1f);
        anim.setDuration(500);
        root.startAnimation(anim);
    }

    // ---------------- SIMPLE TEXTWATCHER ----------------

    private abstract static class SimpleTextWatcher implements android.text.TextWatcher {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void afterTextChanged(android.text.Editable s) {}
    }
}