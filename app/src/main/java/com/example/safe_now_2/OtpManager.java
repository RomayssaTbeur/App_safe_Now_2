package com.example.safe_now_2;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

/**
 * Gestionnaire OTP basé sur Firebase Phone Auth.
 */
public class OtpManager {

    private static final String TAG = "OtpManager";

    private static OtpManager instance;
    public static OtpManager getInstance() {
        if (instance == null) instance = new OtpManager();
        return instance;
    }


    private FirebaseAuth mAuth;

    private OtpManager() {
        mAuth = FirebaseAuth.getInstance();
    }
    /**private OtpManager() {
        mAuth = FirebaseAuth.getInstance();

        if (android.os.Build.FINGERPRINT.contains("generic")) {

            // Désactive reCAPTCHA
            mAuth.getFirebaseAuthSettings()
                    .setAppVerificationDisabledForTesting(true);

            // 🔥 CRUCIAL : évite complètement la page web
            mAuth.getFirebaseAuthSettings()
                    .setAutoRetrievedSmsCodeForPhoneNumber("+212600000001", "123456");
        }
    } **/
    private String verificationId;
    private PhoneAuthProvider.ForceResendingToken resendToken;

    // ── Interfaces callback ────────────────────────────────────────────────────
    public interface SendCallback {
        void onSuccess();
        void onError(String message);
    }

    public interface VerifyCallback {
        void onSuccess();
        void onError(String message);
    }

    // ── Envoi OTP ─────────────────────────────────────────────────────────────
    public void sendOtp(String phoneNumber, Activity activity, SendCallback callback) {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber(phoneNumber)
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(activity)
                        .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                            @Override
                            public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                                // Auto-retrieval succeeded
                                Log.d(TAG, "Verification auto-completée.");
                            }

                            @Override
                            public void onVerificationFailed(@NonNull FirebaseException e) {
                                Log.e(TAG, "Erreur verification OTP: " + e.getMessage());
                                callback.onError("Échec de l'envoi OTP : " + e.getMessage());
                            }

                            @Override
                            public void onCodeSent(@NonNull String verifId,
                                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {
                                verificationId = verifId;
                                resendToken = token;
                                Log.d(TAG, "OTP envoyé avec ID: " + verifId);
                                callback.onSuccess();
                            }
                        })
                        .build();

        PhoneAuthProvider.verifyPhoneNumber(options);
    }


    // ── Vérification OTP ──────────────────────────────────────────────────────
    public void verifyOtp(String code, VerifyCallback callback) {
        if (verificationId == null) {
            callback.onError("Aucun code envoyé. Veuillez recommencer.");
            return;
        }

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        verificationId = null; // Invalider après usage
                        callback.onSuccess();
                    } else {
                        callback.onError("Code incorrect. Veuillez réessayer.");
                    }
                });
    }

    // ── Invalider le code courant ─────────────────────────────────────────────
    public void invalidate() {
        verificationId = null;
        resendToken = null;
    }

    // ── Obtenir le token pour renvoi (optionnel) ─────────────────────────────
    public PhoneAuthProvider.ForceResendingToken getResendToken() {
        return resendToken;
    }
}