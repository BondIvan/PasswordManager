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

        // Создание экземплера шифра AES
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec);

        // Конкатенация IV и зашифрованных данных
        byte[] encrypted = cipher.doFinal(password.getBytes());
        byte[] concatenatedIvAndEncrypted = new byte[iv.length + encrypted.length];
        // Массив (arr1) источник | с какой позиции начать в arr1 | куда скопировать (arr2) | с какой позиции (arr2) начинать вставку | количество элементов, которые нужно вставить
        System.arraycopy(iv, 0, concatenatedIvAndEncrypted, 0, iv.length);
        System.arraycopy(encrypted, 0, concatenatedIvAndEncrypted, iv.length, encrypted.length);

        String base64View = Base64.getEncoder().encodeToString(concatenatedIvAndEncrypted);

        storage.saveKey(keyStore, alias, key, keyStorePassword.toCharArray());

        // Очистка чувствиельных данных из памяти
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

        // Очистка чувствиельных данных из памяти
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

        // Указывает алгоритм, для которого предназначен ключ
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
