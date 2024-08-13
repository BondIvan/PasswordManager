package com.manager.passwordmanager.services.encryption.KeysStorage;

import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.Arrays;

@Component
public class Storage {

    private static final String PATH_TO_KEY_STORE = "src/main/resources/files/KeysStorage.ks";

    public KeyStore initializeKeyStore(String keyStorePassword) throws KeyStoreException {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");

        char[] storePassword = keyStorePassword.toCharArray();
        try {
            if(Files.exists(Paths.get(PATH_TO_KEY_STORE))) {
                try (FileInputStream fileInputStream = new FileInputStream(PATH_TO_KEY_STORE)) {
                    keyStore.load(fileInputStream, storePassword);
                }
            } else {
                // If the keyStore has not yet been created, create it empty
                keyStore.load(null, storePassword);
                try (FileOutputStream fileOutputStream = new FileOutputStream(PATH_TO_KEY_STORE)) {
                    keyStore.store(fileOutputStream, storePassword);
                }
            }
        } catch (CertificateException | NoSuchAlgorithmException | IOException e) {
            throw new RuntimeException(e);
        }
        finally {
            // Clearing sensitive data from memory
            Arrays.fill(storePassword, '\0');
        }

        return keyStore;
    }

    public void saveKey(KeyStore keyStore, String aliasID, SecretKey key, char[] storePassword) throws KeyStoreException {
        KeyStore.SecretKeyEntry secretKeyEntry = new KeyStore.SecretKeyEntry(key);
        KeyStore.ProtectionParameter protectionParameter = new KeyStore.PasswordProtection(storePassword);
        keyStore.setEntry(aliasID, secretKeyEntry, protectionParameter);

        try (FileOutputStream fileOutputStream = new FileOutputStream(PATH_TO_KEY_STORE)) {
            keyStore.store(fileOutputStream, storePassword);
        } catch (CertificateException | NoSuchAlgorithmException | IOException e) {
            throw new RuntimeException(e);
        }
        finally {
            // Clearing sensitive data from memory
            Arrays.fill(storePassword, '\0');
            secretKeyEntry = null;
        }

    }

    public SecretKey loadKey(KeyStore keyStore, String aliasID, char[] storePassword) throws KeyStoreException {
        KeyStore.ProtectionParameter protectionParameter = new KeyStore.PasswordProtection(storePassword);
        KeyStore.SecretKeyEntry secretKeyEntry = null;
        try {
            secretKeyEntry = (KeyStore.SecretKeyEntry) keyStore.getEntry(aliasID, protectionParameter);
        } catch (NoSuchAlgorithmException | UnrecoverableEntryException e) {
            throw new RuntimeException(e);
        }

        SecretKey secretKey = secretKeyEntry.getSecretKey();

        // Clearing sensitive data from memory
        Arrays.fill(storePassword, '\0');
        secretKeyEntry = null;

        return secretKey;
    }

    public void deleteKey(KeyStore keyStore, String aliasID, char[] storePassword) throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
        if(!keyStore.containsAlias(aliasID))
            throw new KeyStoreException("There is no such alias in keyStore");

        keyStore.deleteEntry(aliasID);

        try (FileOutputStream fileOutputStream = new FileOutputStream(PATH_TO_KEY_STORE)) {
            keyStore.store(fileOutputStream, storePassword);
        } finally {
            // Clearing sensitive data from memory
            Arrays.fill(storePassword, '\0');
        }
    }

}
