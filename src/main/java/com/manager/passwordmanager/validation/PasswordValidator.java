package com.manager.passwordmanager.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordValidator implements ConstraintValidator<Password, String> {

    @Override
    public void initialize(Password constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {

        if(s.length() < 8)
            return false;

        String regexUpperCase = ".*\\p{Lu}.*"; // Any language
        String regexLowerCase = ".*\\p{Ll}.*"; // Any language
        String regexSpecialSymbol = ".*[!@#$%^&*()_+\\-=\\[\\]{};:\"\\\\|,.<>/?].*";
        String regexNumber = ".*\\d.*";

        return s.matches(regexUpperCase) && s.matches(regexLowerCase) && s.matches(regexSpecialSymbol) && s.matches(regexNumber);
    }
}
