package com.manager.passwordmanager.services.masterPassword;

import org.springframework.stereotype.Component;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

@Component
public class ValidationMP {

    private final String PATH_SALT = "src/main/resources/files/Salt.bin";
    private final String PATH_VALIDATION = "src/main/resources/files/Validation.bin";
    private static final int AES_KEY_SIZE = 256;
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;
    private static final int SALT_LENGTH = 16;

    // Создать мастер-пароль
    public void createMasterPassword(String inputNewPassword) throws IOException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {

        char[] newPassword = inputNewPassword.toCharArray();
        if (!Files.exists(Paths.get(PATH_SALT)) || !Files.exists(Paths.get(PATH_VALIDATION))) {
            byte[] salt = generateSalt();
            byte[] validation = createValidation(newPassword, salt, null);

            Files.write(Paths.get(PATH_SALT), salt);
            Files.write(Paths.get(PATH_VALIDATION), validation);

            // Очистка чувствиельных данных из памяти
            Arrays.fill(newPassword, '\0');
            Arrays.fill(salt, (byte) '\0');
            Arrays.fill(validation, (byte) '\0');

            System.out.println("Master password created successfully");
        }

    }

    public boolean checkInputPassword(String inputPassword) throws NoSuchPaddingException, IllegalBlockSizeException, IOException, NoSuchAlgorithmException, InvalidKeySpecException, BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {

        char[] inputForValidate = inputPassword.toCharArray();
        byte[] salt = Files.readAllBytes(Paths.get(PATH_SALT));
        byte[] validation = Files.readAllBytes(Paths.get(PATH_VALIDATION));
        byte[] iv = Arrays.copyOfRange(validation, 0, GCM_IV_LENGTH);

        byte[] unverified = createValidation(inputForValidate, salt, iv);

        boolean result = Arrays.equals(validation, unverified);

        // Очистка чувствиельных данных из памяти
        Arrays.fill(inputForValidate, '\0');
        Arrays.fill(salt, (byte) '\0');
        Arrays.fill(validation, (byte) '\0');
        Arrays.fill(iv, (byte) '\0');
        Arrays.fill(unverified, (byte) '\0');

        return result;
    }

    public boolean isExist() {

        return Files.exists(Paths.get(PATH_SALT)) && Files.exists(Paths.get(PATH_VALIDATION));
    }

    private byte[] createValidation(char[] password, byte[] salt, byte[] IV) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {

        // Генерация ключа на основе мастер-пароля и соли
        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        PBEKeySpec spec = new PBEKeySpec(password, salt, 65536, AES_KEY_SIZE);
        byte[] key = secretKeyFactory.generateSecret(spec).getEncoded();
        SecretKey keySpec = new SecretKeySpec(key, "AES");

        // Создание IV, нужно для алгоритма AES в режиме работы GCM
        byte[] iv = (IV == null) ? generateIV() : IV;

        // Создание экземплера шифра AES
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);

        // Шифрование известного значения - "validation";
        byte[] encryptedValidation = cipher.doFinal("validation".getBytes(StandardCharsets.UTF_8));

        // Конкатенация IV и зашифрованных данных
        byte[] resultValidation = new byte[iv.length + encryptedValidation.length];
        System.arraycopy(iv, 0, resultValidation, 0, iv.length); // В пустой массив resultValidation скопировать все байты из iv
        // Массив (arr1) источник | с какой позиции начать в arr1 | куда скопировать (arr2) | с какой позиции (arr2) начинать вставку | количество элементов, которые нужно вставить
        System.arraycopy(encryptedValidation, 0, resultValidation, iv.length, encryptedValidation.length);

        // Очистка чувствиельных данных из памяти
        Arrays.fill(key, (byte) '\0');;
        Arrays.fill(password, '\0');
        spec.clearPassword();
        keySpec = null;

        return resultValidation;
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
