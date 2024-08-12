package com.manager.passwordmanager.validation;

import com.manager.passwordmanager.entity.Note;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PasswordValidationTest {

    private Validator validator;

    @BeforeEach
    public void setUp() {
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @Test
    public void testValidPassword() {

        Note note = new Note(1L, "testName", "https://test.com", "test@gmail.com", null);
        note.setPassword("@abcdef123A^");

        Set<ConstraintViolation<Note>> violations  = validator.validate(note);

        assertEquals(0, violations.size());
    }

    @Test
    public void testInvalidPassword_noUpperCase() {

        Note note = new Note(1L, "testName", "https://test.com", "test@gmail.com", null);
        note.setPassword("+abcdef123abcd^");

        Set<ConstraintViolation<Note>> violations  = validator.validate(note);

        assertEquals(1, violations.size());
    }

    @Test
    public void testInvalidPassword_noLowerCase() {

        Note note = new Note(1L, "testName", "https://test.com", "test@gmail.com", null);
        note.setPassword("+ABCD123ABCD");

        Set<ConstraintViolation<Note>> violations  = validator.validate(note);

        assertEquals(1, violations.size());
    }

    @Test
    public void testInvalidPassword_noSpecialSymbol() {

        Note note = new Note(1L, "testName", "https://test.com", "test@gmail.com", null);
        note.setPassword("abcdef123ABCD");

        Set<ConstraintViolation<Note>> violations  = validator.validate(note);

        assertEquals(1, violations.size());
    }

    @Test
    public void testInvalidPassword_noNumbers() {

        Note note = new Note(1L, "testName", "https://test.com", "test@gmail.com", null);
        note.setPassword("+abcdefABCD");

        Set<ConstraintViolation<Note>> violations  = validator.validate(note);

        assertEquals(1, violations.size());
    }

    @Test
    public void testInvalidPassword_tooShort() {

        Note note = new Note(1L, "testName", "https://test.com", "test@gmail.com", null);
        note.setPassword("aD1-d1f");

        Set<ConstraintViolation<Note>> violations  = validator.validate(note);

        assertEquals(1, violations.size());
    }

    @Test
    public void testInvalidPassword_noNumbers_noSpecialSymbol_tooShort() {

        Note note = new Note(1L, "testName", "https://test.com", "test@gmail.com", null);
        note.setPassword("aB");

        Set<ConstraintViolation<Note>> violations  = validator.validate(note);

        assertEquals(3, violations.size());
    }


}
