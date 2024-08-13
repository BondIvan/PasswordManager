package com.manager.passwordmanager.services;

import com.manager.passwordmanager.services.masterPassword.ValidationMP;
import org.springframework.stereotype.Service;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

@Service
public class MPService {

    private final ValidationMP validationMP;

    public MPService(ValidationMP validationMP) {
        this.validationMP = validationMP;
    }

    public boolean mpExists() {

        return validationMP.isExist();
    }

    public boolean checkMP(String password) {

        try {
            return validationMP.checkInputPassword(password);
        } catch (NoSuchPaddingException | IllegalBlockSizeException | IOException | NoSuchAlgorithmException |
                 InvalidAlgorithmParameterException | InvalidKeyException | InvalidKeySpecException |
                 BadPaddingException e) {
            throw new RuntimeException("Master password verification failed ", e);
        }

    }

    public void createMP(String password) {

        try {
            validationMP.createMasterPassword(password);
        } catch (IOException | NoSuchPaddingException | IllegalBlockSizeException | NoSuchAlgorithmException |
                 InvalidKeySpecException | BadPaddingException | InvalidKeyException |
                 InvalidAlgorithmParameterException e) {
            throw new RuntimeException("Error creating master password" , e);
        }

    }

}
