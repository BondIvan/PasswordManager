package com.manager.passwordmanager.services;

import com.manager.passwordmanager.entity.Note;
import com.manager.passwordmanager.exceptions.DuplicateNoteException;
import com.manager.passwordmanager.repositories.NoteRepository;
import com.manager.passwordmanager.services.encryption.AES;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NoteServiceTest {

    @Mock
    private NoteRepository noteRepository;
    @Mock
    private AES aes;
    private NoteService underTest;

    @BeforeEach
    void setUp() {
        underTest = new NoteService(noteRepository, aes);
    }

    @Test
    void canGetAllNote() {
        // when
        underTest.getAllNote();

        // then
        verify(noteRepository).findAll();
    }

    @Test
    void canAddNewNoteSuccess() throws Exception {

        // given
        Note note = new Note(
                1L,
                "Google",
                "google.com",
                "google@gmail.com",
                "testPassword"
        );

        // when
        when(noteRepository.save(note)).thenReturn(note);
        when(aes.encrypt("testPassword", "Google:1")).thenReturn("encryptedPassword");

        underTest.addNewNote(note);

        // then
        ArgumentCaptor<Note> noteCaptor = ArgumentCaptor.forClass(Note.class);
        verify(noteRepository).save(noteCaptor.capture());
        Note capturedNote = noteCaptor.getValue();

        assertSame(note, capturedNote);

        verify(noteRepository).save(any(Note.class));
        verify(aes).encrypt("testPassword", "Google:1");

        assertEquals("encryptedPassword", note.getPassword());
    }

    @Test
    void cannotAddDuplicateNoteWithEqualsNameAndLogin() throws Exception {

        // given
        Note firstNote = new Note(
                1L,
                "Google",
                "google.com",
                "google@gmail.com",
                "GoogleTestPassword"
        );
        Note duplicate = new Note(
                2L,
                "Google",
                "googleDuplicate.com",
                "google@gmail.com",
                "GoogleDuplicateTestPassword"
        );

        // when
        when(noteRepository.save(duplicate)).thenAnswer(invocation -> {
            Note invoceNote = invocation.getArgument(0);
            String firstNoteName = firstNote.getServiceName();
            String firstNoteLogin = firstNote.getLogin();
            if(firstNoteName.equals(invoceNote.getServiceName())
                    && firstNoteLogin.equals(invoceNote.getLogin())) {
                throw new DataIntegrityViolationException("This is a duplicate");
            }

            return invoceNote;
        });

        // then
        DuplicateNoteException exception = assertThrows(DuplicateNoteException.class, () -> underTest.addNewNote(duplicate));

        verify(noteRepository).save(duplicate);
        verify(aes, never()).encrypt(anyString(), anyString());

        assertEquals("Note with the same service name and login already exists", exception.getMessage());

    }

    @Test
    void cannotAddNewNoteDueEncryptionException() throws Exception {

        // given
        Note note = new Note(
                1L,
                "Google",
                "google.com",
                "google@gmail.com",
                "GoogleTestPassword"
        );

        // when
        when(noteRepository.save(note)).thenReturn(note);
        when(aes.encrypt(anyString(), anyString())).thenThrow(new NoSuchAlgorithmException());

        // then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> underTest.addNewNote(note));
        assertInstanceOf(NoSuchAlgorithmException.class, exception.getCause());

        verify(noteRepository).save(note);
        verify(aes).encrypt("GoogleTestPassword", "Google:1");

    }

    @Test
    void shouldRollbackTransactionWhenHasAnyExceptions() throws Exception {

        // given
        Note note = new Note(
                1L,
                "Google",
                "google.com",
                "google@gmail.com",
                "GoogleTestPassword"
        );

        // when
        when(noteRepository.save(note)).thenReturn(note);
        when(aes.encrypt(anyString(), anyString())).thenThrow(new NoSuchAlgorithmException());

        // then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> underTest.addNewNote(note));
        assertInstanceOf(NoSuchAlgorithmException.class, exception.getCause());

        verify(noteRepository).save(note);
        verify(aes).encrypt("GoogleTestPassword", "Google:1");

        verifyNoMoreInteractions(noteRepository);
    }

    @Test
    @Disabled
    void getNoteById() {
    }

    @Test
    @Disabled
    void decryptPassword() {
    }

    @Test
    @Disabled
    void deleteNoteById() {
    }
}