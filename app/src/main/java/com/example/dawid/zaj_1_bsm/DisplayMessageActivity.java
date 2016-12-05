package com.example.dawid.zaj_1_bsm;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;

public class DisplayMessageActivity extends AppCompatActivity {
    private EditText messageEditText;
    private Security security;
    private String defaultMessage = "Default message.";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_message);

        this.security = new Security(this);

        SharedPreferences messagePrefs = getSharedPreferences(Constants.MESSAGE_PREFS, 0);

        messageEditText = (EditText) findViewById(R.id.messageEditText);

        if (messagePrefs.getString(Constants.MESSAGE_PREFS, defaultMessage).equals(defaultMessage)) {
            messageEditText.setText(defaultMessage, TextView.BufferType.EDITABLE);
        } else {
            byte[] message = Base64.decode(messagePrefs.getString(Constants.MESSAGE_PREFS, null), Base64.DEFAULT);
            byte[] decryptedMessage = security.decrypt(message, Security.IV_MESSAGE_ALIAS);// fill
            char[] chars = security.toChars(decryptedMessage);// fill
            Arrays.fill(decryptedMessage, (byte) 0);
            messageEditText.setText(chars, 0, chars.length);
            Arrays.fill(chars, '\u0000'); //clear data
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        messageEditText.setText(""); //clear sensitive
        EditText editText = (EditText) findViewById(R.id.newPasswordEditText);
        editText.setText("");

    }

    void changePassword(View view) {
        EditText editText = (EditText) findViewById(R.id.newPasswordEditText);
        byte[] newPassword = security.getBytesSecurely(editText);

        if (newPassword.length < 12)
            Toast.makeText(getApplicationContext(), "Password is to short. Is should have at least 12 characters.",
                    Toast.LENGTH_LONG).show();
        else {
            SharedPreferences password = getSharedPreferences(Constants.PASS_PREFS, 0);
            SharedPreferences.Editor editor = password.edit();
            String encoded = Base64.encodeToString(security.encrypt(newPassword, Security.IV_PASSWORD_ALIAS), Base64.DEFAULT);
            editor.putString(Constants.PASS_PREFS, encoded);
            editor.apply();
            Toast.makeText(getApplicationContext(), "Password changed", Toast.LENGTH_SHORT).show();
        }
        Arrays.fill(newPassword, (byte) 0); //clear sensitive data
    }

    void saveMessage(View view) {
        SharedPreferences messagePrefs = getSharedPreferences(Constants.MESSAGE_PREFS, 0);
        SharedPreferences.Editor editor = messagePrefs.edit();
        byte[] message = security.getBytesSecurely(messageEditText);

        if (message.length > 0) {
            String encoded = Base64.encodeToString(security.encrypt(message, Security.IV_MESSAGE_ALIAS), Base64.DEFAULT);
            Arrays.fill(message, (byte) 0); //clear data
            editor.putString(Constants.MESSAGE_PREFS, encoded);
            editor.apply();
            Toast.makeText(getApplicationContext(), "Message saved", Toast.LENGTH_SHORT).show();
        }
    }

}
