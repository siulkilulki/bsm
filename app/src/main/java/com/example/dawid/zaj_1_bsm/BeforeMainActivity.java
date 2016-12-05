package com.example.dawid.zaj_1_bsm;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import java.util.Arrays;

public class BeforeMainActivity extends AppCompatActivity {

    private Security security;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_before_main);
        SharedPreferences passwordPrefs = getSharedPreferences(Constants.PASS_PREFS, 0);
        String password = passwordPrefs.getString(Constants.PASS_PREFS, "None"); //encrypted so safe to be in String
        if (!password.equals("None")) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
        security = new Security(this);
    }

    void createFirstPassword(View view) throws Exception {
        EditText editText = (EditText) findViewById(R.id.firstPasswordField);

        byte[] newPassword = security.getBytesSecurely(editText);

        if (newPassword.length < 12)
            Toast.makeText(getApplicationContext(), "Password is to short. Is should have at least 12 characters.",
                    Toast.LENGTH_LONG).show();
        else {
            security.createSecretKey();

            SharedPreferences sharedPreferences = getSharedPreferences(Constants.PASS_PREFS, 0);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            String encoded = Base64.encodeToString(security.encrypt(newPassword, Security.IV_PASSWORD_ALIAS), Base64.DEFAULT);

            //clear sensitive data
            Arrays.fill(newPassword, (byte) 0);

            editor.putString(Constants.PASS_PREFS, encoded);
            editor.apply();
            Toast.makeText(getApplicationContext(), "Password created.", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
    }
}
