package com.manager.passwordmanager.services;

import com.manager.passwordmanager.entity.Note;
import com.manager.passwordmanager.exceptions.DuplicateNoteException;
import com.manager.passwordmanager.exceptions.NotFoundException;
import com.manager.passwordmanager.repositories.NoteRepository;
import com.manager.passwordmanager.services.encryption.AES;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

@Service
public class NoteService {

    private final NoteRepository noteRepository;
    private final AES aes;

    public NoteService(NoteRepository noteRepository, AES aes) {
        this.noteRepository = noteRepository;
        this.aes = aes;
    }

    public List<Note> getAllNote() {
        return noteRepository.findAll();
    }

    public Note getNoteById(Long id) {
        return noteRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Note with id = " + id + " not found"));
    }

    public String decryptPassword(Note note) {
        try {
            String alias = note.getServiceName() + ":" + note.getId();
            String password = note.getPassword();
            return aes.decrypt(password, alias);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | IllegalBlockSizeException |
                 BadPaddingException | InvalidAlgorithmParameterException | KeyStoreException e) {
            throw new RuntimeException("Error decrypting password", e);
        }
    }

    @Transactional
    public void addNewNote(Note note) {
        try {
            Note savedNote = noteRepository.save(note);

            String openPassword = savedNote.getPassword();
            String alias = note.getServiceName() + ":" + savedNote.getId();
            String encrypt = aes.encrypt(openPassword, alias);
            note.setPassword(encrypt);

        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateNoteException("Note with the same service name and login already exists", ex);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidAlgorithmParameterException |
                 InvalidKeyException | IllegalBlockSizeException | BadPaddingException | InvalidKeySpecException |
                 KeyStoreException e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public void deleteNoteById(Long id) {
        try {

            Note note = getNoteById(id);
            noteRepository.deleteById(id);

            String alias = note.getServiceName() + ":" + note.getId();
            aes.deleteAlias(alias);

        } catch (KeyStoreException | CertificateException | IOException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e.getMessage());
        }

    }

    public Note getNoteByServiceName(String serviceName) {
        return null;
    }

    public void updateNote(Note note) {

    }

}
