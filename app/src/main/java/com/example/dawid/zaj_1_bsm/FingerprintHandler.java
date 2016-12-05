package com.example.dawid.zaj_1_bsm;

import android.content.Context;
import android.content.Intent;
import android.hardware.fingerprint.FingerprintManager;
import android.os.CancellationSignal;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * Created by Justynka on 05.12.2016.
 */

public class FingerprintHandler extends FingerprintManager.AuthenticationCallback {

    Context context;
    ImageView imageView;
    Button button;

    public FingerprintHandler(Context context, ImageView imageView, Button button) {
        this.context = context;
        this.imageView = imageView;
        this.button = button;
    }

    @Override
    public void onAuthenticationError(int errorCode, CharSequence errString) {
        super.onAuthenticationError(errorCode, errString);
        Toast.makeText(context, "Authentication error", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
        super.onAuthenticationHelp(helpCode, helpString);
    }

    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
        super.onAuthenticationSucceeded(result);
        Toast.makeText(context, "Authenticated", Toast.LENGTH_LONG).show();
        imageView.setVisibility(View.GONE);
        button.setEnabled(true);
        Intent intent = new Intent(context, DisplayMessageActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    public void onAuthenticationFailed() {
        super.onAuthenticationFailed();
        Toast.makeText(context, "Fingerprint not matched!", Toast.LENGTH_LONG).show();
    }

    public void doAuth(FingerprintManager manager,
                       FingerprintManager.CryptoObject obj) {
        CancellationSignal signal = new CancellationSignal();

        try {
            manager.authenticate(obj, signal, 0, this, null);
        }
        catch(SecurityException sce) {}
    }
}
