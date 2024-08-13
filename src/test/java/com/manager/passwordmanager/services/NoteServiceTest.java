package com.manager.passwordmanager.services;

import com.manager.passwordmanager.entity.Note;
import com.manager.passwordmanager.exceptions.DuplicateNoteException;
import com.manager.passwordmanager.exceptions.NotFoundException;
import com.manager.passwordmanager.repositories.NoteRepository;
import com.manager.passwordmanager.services.encryption.AES;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

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
    void shouldRollbackTransactionWhenAddNoteHasAnyExceptions() throws Exception {

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
    void canDeleteNoteById() throws Exception {

        // given
        Note note = new Note(
                1L,
                "Google",
                "google.com",
                "google@gmail.com",
                "testPassword"
        );
        Long id = note.getId();
        String alias = note.getServiceName() + ":" + id;

        // when
        when(noteRepository.findById(id)).thenReturn(Optional.of(note));
        underTest.deleteNoteById(id);

        // then
        ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
        verify(noteRepository).findById(idCaptor.capture());
        verify(noteRepository).deleteById(idCaptor.capture());

        Long receivedId = idCaptor.getValue();
        assertEquals(id, receivedId);

        verify(aes).deleteAlias(alias);
    }

    @Test
    void cannotDeleteNoteByIdWhenNoteDontExist() throws Exception {

        // given
        Long id = 1L;

        // when
        when(noteRepository.findById(id)).thenReturn(Optional.empty());
        NotFoundException exception = assertThrows(NotFoundException.class, () -> underTest.deleteNoteById(id));

        // then
        ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
        verify(noteRepository).findById(idCaptor.capture());

        verify(noteRepository, never()).deleteById(idCaptor.capture());
        verify(aes, never()).deleteAlias(anyString());

        Long receivedId = idCaptor.getValue();
        assertEquals(id, receivedId);

        assertEquals("Note with id = " + id + " not found", exception.getMessage());
    }

    @Test
    void cannotDeleteNoteByIdDueEncryptionException() throws Exception {

        // given
        Note note = new Note(
                1L,
                "Google",
                "google.com",
                "google@gmail.com",
                "testPassword"
        );
        Long id = note.getId();
        String alias = note.getServiceName() + ":" + id;


        // when
        doThrow(new KeyStoreException("There is no such alias in keyStore")).when(aes).deleteAlias(alias);

        when(noteRepository.findById(id)).thenReturn(Optional.of(note));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> underTest.deleteNoteById(id));

        // then
        ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);

        verify(noteRepository).findById(idCaptor.capture());
        verify(noteRepository).deleteById(idCaptor.capture());

        verify(aes).deleteAlias(alias);

        Long receivedId = idCaptor.getValue();
        assertEquals(id, receivedId);

        assertInstanceOf(KeyStoreException.class, exception.getCause());
        assertEquals("There is no such alias in keyStore", exception.getMessage());
    }

    @Test
    void shouldRollbackTransactionWhenDeleteNoteHasAnyExceptions() throws Exception {

        // given
        Note note = new Note(
                1L,
                "Google",
                "google.com",
                "google@gmail.com",
                "testPassword"
        );
        Long id = note.getId();
        String alias = note.getServiceName() + ":" + id;

        // when
        doThrow(new KeyStoreException("There is no such ID in keyStore")).when(aes).deleteAlias(alias);

        when(noteRepository.findById(id)).thenReturn(Optional.of(note));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> underTest.deleteNoteById(id));

        // then
        ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);

        verify(noteRepository).findById(idCaptor.capture());
        verify(noteRepository).deleteById(idCaptor.capture());

        verify(aes).deleteAlias(alias);

        Long receivedId = idCaptor.getValue();
        assertEquals(id, receivedId);

        assertInstanceOf(KeyStoreException.class, exception.getCause());
        assertEquals("There is no such ID in keyStore", exception.getMessage());

        verifyNoMoreInteractions(noteRepository);
    }

    @Test
    void canGetNoteById() {

        // given
        Note note = new Note(
                1L,
                "Google",
                "google.com",
                "google@gmail.com",
                "testPassword"
        );

        Long id = note.getId();

        // when
        when(noteRepository.findById(id)).thenReturn(Optional.of(note));
        Note receivedNote = underTest.getNoteById(id);

        // then
        ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
        verify(noteRepository).findById(idCaptor.capture());

        Long capturedId = idCaptor.getValue();

        assertEquals(id, capturedId);
        assertSame(note, receivedNote);
    }

    @Test
    void cannotGetNoteById() {

        // given
        Long id = 1L;

        // when
        when(noteRepository.findById(id)).thenReturn(Optional.empty());
        NotFoundException exception = assertThrows(NotFoundException.class, () -> underTest.getNoteById(id));

        // then
        verify(noteRepository).findById(id);

        assertEquals("Note with id = " + id + " not found", exception.getMessage());
    }

    @Test
    void shouldDecryptPassword() throws Exception {

        // given
        Note note = new Note(
                1L,
                "Google",
                "google.com",
                "google@gmail.com",
                "encryptedPassword"
        );
        String alias = note.getServiceName() + ":" + note.getId();
        String decryptedPassword = "decryptedPassword";

        // when
        when(aes.decrypt(note.getPassword(), alias)).thenReturn(decryptedPassword);
        String underDecrypted = underTest.decryptPassword(note);

        // then
        String notePassword = note.getPassword();

        verify(aes).decrypt(notePassword, alias);

        assertEquals(decryptedPassword, underDecrypted);

    }

    @Test
    void shouldNotDecryptPassword() throws Exception {

        // given
        Note note = new Note(
                1L,
                "Google",
                "google.com",
                "google@gmail.com",
                "encryptedPassword"
        );
        String alias = note.getServiceName() + ":" + note.getId();

        // when
        doThrow(new KeyStoreException()).when(aes).decrypt(note.getPassword(), alias);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> underTest.decryptPassword(note));

        // then
        String notePassword = note.getPassword();

        verify(aes).decrypt(notePassword, alias);

        assertInstanceOf(KeyStoreException.class, exception.getCause());
        assertEquals("Error decrypting password", exception.getMessage());
    }
}