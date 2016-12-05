package com.example.dawid.zaj_1_bsm;

import android.app.KeyguardManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.fingerprint.FingerprintManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import javax.crypto.spec.SecretKeySpec;
import java.util.Arrays;

import static android.provider.AlarmClock.EXTRA_MESSAGE;

public class MainActivity extends AppCompatActivity {

    private Security security;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.security = new Security(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        EditText editText = (EditText) findViewById(R.id.passwordField);
        editText.setText("");
    }

    void logIn(View view) {
        EditText editText = (EditText) findViewById(R.id.passwordField);
        byte[] message = security.getBytesSecurely(editText);

        SharedPreferences passwordPrefs = getSharedPreferences(Constants.PASS_PREFS, 0);
        byte[] password = Base64.decode(passwordPrefs.getString(Constants.PASS_PREFS, ""), Base64.DEFAULT);

        if (Arrays.equals(security.decrypt(password, Security.IV_PASSWORD_ALIAS), message)) {
            Arrays.fill(message, (byte) 0); //clear sensitive data
            Arrays.fill(password, (byte) 0); //clear sensitive data
            Toast.makeText(getApplicationContext(), "Successful login", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, DisplayMessageActivity.class);
            startActivity(intent);
        } else {
            Arrays.fill(message, (byte) 0); //clear sensitive data
            Arrays.fill(password, (byte) 0); //clear sensitive data
            Toast.makeText(getApplicationContext(), "Wrong password", Toast.LENGTH_LONG).show();
        }
    }

    void fingerprintLogIn(View view) {
        Toast.makeText(getApplicationContext(), "Fingerprint", Toast.LENGTH_LONG).show();
    }


    private boolean checkFinger() {

        // Keyguard Manager
        KeyguardManager keyguardManager = (KeyguardManager)
                getSystemService(KEYGUARD_SERVICE);

        // Fingerprint Manager
        FingerprintManager fingerprintManager = (FingerprintManager)
                getSystemService(FINGERPRINT_SERVICE);

        try {
            // Check if the fingerprint sensor is present
            if (!fingerprintManager.isHardwareDetected()) {
                // Update the UI with a message
                Toast.makeText(getApplicationContext(), "Fingerprint authentication not supported", Toast.LENGTH_LONG).show();
                return false;
            }

            if (!fingerprintManager.hasEnrolledFingerprints()) {
                Toast.makeText(getApplicationContext(), "No fingerprint configured.", Toast.LENGTH_LONG).show();
                return false;
            }

            if (!keyguardManager.isKeyguardSecure()) {
                Toast.makeText(getApplicationContext(), "Secure lock screen not enabled", Toast.LENGTH_LONG).show();
                return false;
            }
        }
        catch(SecurityException se) {
            se.printStackTrace();
        }
        return true;
    }
}
