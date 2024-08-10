package com.manager.passwordmanager.services.encryption;

import com.manager.passwordmanager.services.encryption.KeysStorage.Storage;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.DestroyFailedException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Base64;

@Component
public class AES {

    private static final int AES_KEY_SIZE = 256;
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;
    private static final int SALT_LENGTH = 16;

    private final Storage storage;
    private KeyStore keyStore;

    @Value("${keystore.password}")
    private String keyStorePassword;

    public AES(Storage storage) {
        this.storage = storage;
    }

    @PostConstruct
    private void init() throws KeyStoreException {
        this.keyStore = storage.initializeKeyStore(keyStorePassword);
    }

    public String encrypt(String password, String alias) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException, KeyStoreException {

        byte[] salt = generateSalt();
        byte[] iv = generateIV();
        SecretKey key = generateKey(password.toCharArray(), salt);

        // Creating an AES Cipher Instance
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec);

        // Concatenation of IV and encrypted data
        byte[] encrypted = cipher.doFinal(password.getBytes());
        byte[] concatenatedIvAndEncrypted = new byte[iv.length + encrypted.length];
        // Array (arr1) source | what position to start in arr1 | where to copy (arr2) | from what position to start (arr2) insertion | number of elements
        System.arraycopy(iv, 0, concatenatedIvAndEncrypted, 0, iv.length);
        System.arraycopy(encrypted, 0, concatenatedIvAndEncrypted, iv.length, encrypted.length);

        String base64View = Base64.getEncoder().encodeToString(concatenatedIvAndEncrypted);

        storage.saveKey(keyStore, alias, key, keyStorePassword.toCharArray());

        // Clearing sensitive data from memory
        Arrays.fill(iv, (byte) '\0');
        Arrays.fill(salt, (byte) '\0');
        Arrays.fill(encrypted, (byte) '\0');
        Arrays.fill(concatenatedIvAndEncrypted, (byte) '\0');
        key = null;

        return base64View;
    }

    public String decrypt(String encrypted, String alias) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, DestroyFailedException, KeyStoreException {

        SecretKey key = storage.loadKey(keyStore, alias, keyStorePassword.toCharArray());

        byte[] fromBase64ToByteView = Base64.getDecoder().decode(encrypted);
        byte[] iv = Arrays.copyOfRange(fromBase64ToByteView, 0, GCM_IV_LENGTH);
        byte[] encryptText = Arrays.copyOfRange(fromBase64ToByteView, GCM_IV_LENGTH, fromBase64ToByteView.length);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec);
        byte[] decrypted = cipher.doFinal(encryptText);

        // Clearing sensitive data from memory
        Arrays.fill(fromBase64ToByteView, (byte) '\0');
        Arrays.fill(iv, (byte) '\0');
        Arrays.fill(encryptText, (byte) '\0');
        key = null;

        return new String(decrypted);
    }

    private SecretKey generateKey(char[] password, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password, salt, 65536, AES_KEY_SIZE);
        SecretKey tmp = factory.generateSecret(spec);

        // Specifies the algorithm the key is intended for
        return new SecretKeySpec(tmp.getEncoded(), "AES");
    }

    private byte[] generateSalt() {

        SecureRandom secureRandom = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        secureRandom.nextBytes(salt);

        return salt;
    }

    private byte[] generateIV() {

        SecureRandom secureRandom = new SecureRandom();
        byte[] iv = new byte[GCM_IV_LENGTH];
        secureRandom.nextBytes(iv);

        return iv;
    }

}
