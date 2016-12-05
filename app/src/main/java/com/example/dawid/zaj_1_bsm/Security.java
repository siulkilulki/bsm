package com.example.dawid.zaj_1_bsm;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.widget.EditText;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Arrays;

/**
 * Created by dawid on 06.11.16.
 */
class Security {
    static final String KEY_ALIAS = "keystoreEntryAlias";
    static final String IV_PASSWORD_ALIAS = "initPasswordVector";
    static final String IV_MESSAGE_ALIAS = "initMessageVector";

    private final Context context;

    public Security(Context context) {
        this.context = context;
    }

    void createSecretKey() throws Exception {
        try {
            KeyGenerator kg = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
            kg.init(new KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_DECRYPT | KeyProperties.PURPOSE_ENCRYPT)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    //.setUserAuthenticationRequired(true)
                    //.setUserAuthenticationValidityDurationSeconds(300)
                    .build());
            SecretKey secretKey = kg.generateKey(); //unexportable byte[] key, no need to clear
        } catch (NoSuchAlgorithmException | NoSuchProviderException
                | InvalidAlgorithmParameterException e) {
            throw new RuntimeException("Failed to create a symmetric key", e);
        }
    }

    Cipher getCipherInstance() {
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);

            SecretKey secretKey = (SecretKey) keyStore.getKey(KEY_ALIAS, null);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return cipher;
        }  catch (KeyStoreException |
                CertificateException | UnrecoverableKeyException | IOException
                | NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    byte[] encrypt(byte[] toEncrypt, String ivAlias) {
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);

            SecretKey secretKey = (SecretKey) keyStore.getKey(KEY_ALIAS, null);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            String initializationVector = Base64.encodeToString(cipher.getIV(), Base64.DEFAULT);//does not need to be secret
            SharedPreferences sharedPreferences = context.getSharedPreferences(ivAlias, 0);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(ivAlias, initializationVector);
            editor.apply();

            return cipher.doFinal(toEncrypt);
        } catch (BadPaddingException | IllegalBlockSizeException | KeyStoreException |
                CertificateException | UnrecoverableKeyException | IOException
                | NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    byte[] decrypt(byte[] toDecrypt, String ivAlias) {
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);

            SecretKey secretKey = (SecretKey) keyStore.getKey(KEY_ALIAS, null);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
            SharedPreferences sharedPreferences = context.getSharedPreferences(ivAlias, 0);
            byte[] iv = Base64.decode(sharedPreferences.getString(ivAlias, ""), Base64.DEFAULT);

            cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));

            return cipher.doFinal(toDecrypt);
        } catch (BadPaddingException | IllegalBlockSizeException | KeyStoreException |
                CertificateException | UnrecoverableKeyException | IOException | InvalidAlgorithmParameterException
                | NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }



    char[] toChars(byte[] bytes) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        CharBuffer charBuffer = StandardCharsets.UTF_8.decode(byteBuffer);
        char[] chars = Arrays.copyOfRange(charBuffer.array(), charBuffer.position(), charBuffer.limit());
        Arrays.fill(charBuffer.array(), '\u0000'); // clear sensitive data
        Arrays.fill(byteBuffer.array(), (byte) 0); // clear sensitive data
        return chars;
    }

    byte[] toBytes(char[] chars) {
        CharBuffer charBuffer = CharBuffer.wrap(chars);
        ByteBuffer byteBuffer = StandardCharsets.UTF_8.encode(charBuffer);
        byte[] bytes = Arrays.copyOfRange(byteBuffer.array(), byteBuffer.position(), byteBuffer.limit());
        Arrays.fill(charBuffer.array(), '\u0000'); // clear sensitive data
        Arrays.fill(byteBuffer.array(), (byte) 0); // clear sensitive data
        return bytes;
    }

    byte[] getBytesSecurely(EditText editText) {
        int length = editText.getText().length();
        char[] chars = new char[length];
        editText.getText().getChars(0, length, chars, 0);
        byte[] bytes = toBytes(chars);

        Arrays.fill(chars, '\u0000');     //clear sensitive data
        editText.setText("");            //clear sensitive data

        return bytes;
    }
}
