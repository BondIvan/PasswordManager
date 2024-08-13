package com.manager.passwordmanager.services;

import com.manager.passwordmanager.services.masterPassword.ValidationMP;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MPServiceTest {

    @Mock
    private ValidationMP validationMP;
    private MPService underTest;

    @BeforeEach
    void setUp() {
        underTest = new MPService(validationMP);
    }

    @Test
    void mpExists() {

        // given

        // when
        underTest.mpExists();

        // then
        verify(validationMP).isExist();

    }

    @Test
    void checkMPSuccess() throws Exception {

        // given
        String password = "MPassword";

        // when
        when(validationMP.checkInputPassword(password)).thenReturn(true);
        boolean checkResult = underTest.checkMP(password);

        // then
        verify(validationMP).checkInputPassword(password);

        assertTrue(checkResult);
    }

    @Test
    void checkMPFail() throws Exception {

        // given
        String password = "MPassword";

        // when
        when(validationMP.checkInputPassword(password)).thenReturn(false);

        boolean checkResult = underTest.checkMP(password);

        // then
        verify(validationMP).checkInputPassword(password);

        assertFalse(checkResult);
    }

    @Test
    void cannotCheckMPWhenException() throws Exception {

        // given
        String password = "MPassword";

        // when
        doThrow(new NoSuchAlgorithmException()).when(validationMP).checkInputPassword(password);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> underTest.checkMP(password));

        // then
        verify(validationMP).checkInputPassword(password);

        assertInstanceOf(NoSuchAlgorithmException.class, exception.getCause());
        assertEquals("Master password verification failed ", exception.getMessage());
    }

    @Test
    void createMPSuccess() throws Exception {

        // given
        String password = "MPassword";

        // when
        underTest.createMP(password);

        // then
        verify(validationMP).createMasterPassword(password);

    }

    @Test
    void cannotCreateMPWhenException() throws Exception {

        // given
        String password = "MPassword";

        // when
        doThrow(new NoSuchAlgorithmException()).when(validationMP).createMasterPassword(password);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> underTest.createMP(password));

        // then
        verify(validationMP).createMasterPassword(password);

        assertInstanceOf(NoSuchAlgorithmException.class, exception.getCause());
        assertEquals("Error creating master password", exception.getMessage());
    }
}