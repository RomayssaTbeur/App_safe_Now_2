package com.example.safe_now_2.controller;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.safe_now_2.AuthPreferences;
import com.example.safe_now_2.utils.OtpManager;
import com.example.safe_now_2.R;
import com.google.android.material.button.MaterialButton;

/**
 * Écran de vérification OTP avec Firebase Auth.
 */
public class OtpVerificationActivity extends AppCompatActivity {

    public static final String EXTRA_PHONE = "extra_phone";

    // ── Views ─────────────────────────────────────────────────────────────────
    private EditText[] otpFields;
    private TextView tvPhoneDisplay, tvOtpError, tvResendTimer;
    private Button btnResend;
    private LinearLayout layoutResendTimer;
    private MaterialButton btnVerify;
    private View viewPulseOuter;

    // ── État ──────────────────────────────────────────────────────────────────
    private String phoneNumber;
    private CountDownTimer countDownTimer;
    private static final long RESEND_DELAY_MS = 30_000L;
    private static final long TICK_MS = 1_000L;

    // ── Helpers ───────────────────────────────────────────────────────────────
    private AuthPreferences authPrefs;
    private OtpManager otpManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);

        // Status bar clair
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.surface));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        // Récupérer le numéro passé depuis LoginActivity
        if (savedInstanceState != null) {
            phoneNumber = savedInstanceState.getString(EXTRA_PHONE, "");
        } else {
            phoneNumber = getIntent().getStringExtra(EXTRA_PHONE);
            if (phoneNumber == null) phoneNumber = "";
        }

        authPrefs = new AuthPreferences(this);
        otpManager = OtpManager.getInstance();

        initViews();
        setupOtpNavigation();
        setupListeners();
        startResendTimer();
        animatePulse();

        // Envoyer le code via Firebase dès l'ouverture
        sendOtpFirebase();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(EXTRA_PHONE, phoneNumber);
    }

    // ── Initialisation des vues ───────────────────────────────────────────────
    private void initViews() {
        tvPhoneDisplay = findViewById(R.id.tv_phone_display);
        tvOtpError = findViewById(R.id.tv_otp_error);
        tvResendTimer = findViewById(R.id.tv_resend_timer);
        btnResend = findViewById(R.id.btn_resend);
        layoutResendTimer = findViewById(R.id.layout_resend_timer);
        btnVerify = findViewById(R.id.btn_verify);
        viewPulseOuter = findViewById(R.id.view_pulse_outer);

        tvPhoneDisplay.setText(phoneNumber);

        otpFields = new EditText[]{
                findViewById(R.id.otp_1),
                findViewById(R.id.otp_2),
                findViewById(R.id.otp_3),
                findViewById(R.id.otp_4),
                findViewById(R.id.otp_5),
                findViewById(R.id.otp_6)
        };

        findViewById(R.id.btn_back).setOnClickListener(v -> {
            otpManager.invalidate();
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });
    }

    // ── Navigation entre champs OTP ───────────────────────────────────────────
    private void setupOtpNavigation() {
        for (int i = 0; i < otpFields.length; i++) {
            final int index = i;
            otpFields[i].addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
                @Override public void afterTextChanged(Editable s) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    hideError();
                    if (s.length() == 1 && index < otpFields.length - 1)
                        otpFields[index + 1].requestFocus();

                    if (areAllFieldsFilled()) verifyCode();
                }
            });

            otpFields[i].setOnKeyListener((v, keyCode, event) -> {
                if (event.getAction() == KeyEvent.ACTION_DOWN
                        && keyCode == KeyEvent.KEYCODE_DEL
                        && index > 0
                        && otpFields[index].getText().toString().isEmpty()) {
                    otpFields[index - 1].requestFocus();
                    otpFields[index - 1].setText("");
                    return true;
                }
                return false;
            });
        }
        otpFields[0].requestFocus();
    }

    // ── Listeners ─────────────────────────────────────────────────────────────
    private void setupListeners() {
        btnVerify.setOnClickListener(v -> verifyCode());
        btnResend.setOnClickListener(v -> {
            if (!btnResend.isEnabled()) return;
            clearAllFields();
            sendOtpFirebase();
        });
    }

    // ── Envoi du code via Firebase ────────────────────────────────────────────
    private void sendOtpFirebase() {
        otpManager.sendOtp(phoneNumber, this, new OtpManager.SendCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> startResendTimer());
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() ->
                        Toast.makeText(OtpVerificationActivity.this, message, Toast.LENGTH_SHORT).show());
            }
        });
    }

    // ── Vérification du code ──────────────────────────────────────────────────
    private void verifyCode() {
        String code = getEnteredCode();
        if (code.length() < 6) {
            showError(getString(R.string.error_invalid_otp));
            return;
        }

        setVerifyEnabled(false);

        otpManager.verifyOtp(code, new OtpManager.VerifyCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    authPrefs.setLoggedIn(phoneNumber);
                    goToMain();
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    setVerifyEnabled(true);
                    showError(message);
                    shakeOtpFields();
                });
            }
        });
    }

    private String getEnteredCode() {
        StringBuilder sb = new StringBuilder();
        for (EditText field : otpFields) sb.append(field.getText().toString().trim());
        return sb.toString();
    }

    private boolean areAllFieldsFilled() {
        for (EditText field : otpFields) if (field.getText().toString().isEmpty()) return false;
        return true;
    }

    private void clearAllFields() {
        for (EditText field : otpFields) field.setText("");
        otpFields[0].requestFocus();
    }

    // ── Timer de renvoi ───────────────────────────────────────────────────────
    private void startResendTimer() {
        btnResend.setEnabled(false);
        btnResend.setAlpha(0.4f);
        layoutResendTimer.setVisibility(View.VISIBLE);

        if (countDownTimer != null) countDownTimer.cancel();

        countDownTimer = new CountDownTimer(RESEND_DELAY_MS, TICK_MS) {
            @Override
            public void onTick(long millisUntilFinished) {
                int seconds = (int) (millisUntilFinished / 1000);
                tvResendTimer.setText("Resend in " + seconds + "s");
            }

            @Override
            public void onFinish() {
                layoutResendTimer.setVisibility(View.GONE);
                btnResend.setEnabled(true);
                btnResend.setAlpha(1f);
            }
        }.start();
    }

    // ── Animations ───────────────────────────────────────────────────────────
    private void animatePulse() {
        if (viewPulseOuter == null) return;

        Animation pulse = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, android.view.animation.Transformation t) {
                float scale = 1f + 0.08f * (float) Math.sin(interpolatedTime * 2 * Math.PI);
                viewPulseOuter.setScaleX(scale);
                viewPulseOuter.setScaleY(scale);
                viewPulseOuter.setAlpha(0.4f + 0.1f * (float) Math.sin(interpolatedTime * 2 * Math.PI));
            }
        };
        pulse.setDuration(2000);
        pulse.setRepeatCount(Animation.INFINITE);
        pulse.setRepeatMode(Animation.RESTART);
        viewPulseOuter.startAnimation(pulse);
    }

    private void shakeOtpFields() {
        LinearLayout otpContainer = (LinearLayout) otpFields[0].getParent();
        if (otpContainer == null) return;

        TranslateAnimation shake = new TranslateAnimation(0, 12, 0, 0);
        shake.setDuration(80);
        shake.setRepeatCount(5);
        shake.setRepeatMode(Animation.REVERSE);
        otpContainer.startAnimation(shake);
    }

    private void showError(String msg) {
        tvOtpError.setText(msg);
        tvOtpError.setVisibility(View.VISIBLE);
    }

    private void hideError() {
        tvOtpError.setVisibility(View.GONE);
    }

    private void setVerifyEnabled(boolean enabled) {
        btnVerify.setEnabled(enabled);
        btnVerify.setAlpha(enabled ? 1f : 0.6f);
    }

    private void goToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) countDownTimer.cancel();
    }
}