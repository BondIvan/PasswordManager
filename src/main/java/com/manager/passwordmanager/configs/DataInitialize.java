package com.manager.passwordmanager.configs;

import com.manager.passwordmanager.entity.Note;
import com.manager.passwordmanager.repositories.NoteRepository;
import com.manager.passwordmanager.services.encryption.AES;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;

@Component
public class DataInitialize {

    private final NoteRepository noteRepository;
    private final AES aes;

    public DataInitialize(NoteRepository noteRepository, AES aes) {
        System.out.println("Initialize Data constructor");
        this.noteRepository = noteRepository;
        this.aes = aes;
    }

    @PostConstruct
    public void init() throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeySpecException, KeyStoreException, InvalidKeyException, CertificateException, IOException {

        Note note1 = new Note();
        note1.setServiceName("Google");
        note1.setUrl("Google.com");
        note1.setLogin("Google@gmail.com");

        String encryptedGooglePassword = aes.encrypt("hash-google-password", "google");
        note1.setPassword(encryptedGooglePassword);

        Note note2 = new Note();
        note2.setServiceName("Yandex");
        note2.setUrl("Yandex.ru");
        note2.setLogin("Yandex@yandex.ru");

        String encryptedYandexPassword = aes.encrypt("hash-yandex-password", "yandex");
        note2.setPassword(encryptedYandexPassword);

        Iterable<Note> notes = List.of(note1, note2);
        
        noteRepository.saveAll(notes);
    }

}
