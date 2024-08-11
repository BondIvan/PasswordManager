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

        String regexUpperCase = ".*\\p{Lu}.*"; // Any language
        String regexLowerCase = ".*\\p{Ll}.*"; // Any language
        String regexSpecialSymbol = ".*[!@#$%^&*()_+\\-=\\[\\]{};:\"\\\\|,.<>/?].*";
        String regexNumber = ".*\\d.*";

        boolean isValid = true;

        if(!s.matches(regexUpperCase)) {
            isValid = false;
            constraintValidatorContext.buildConstraintViolationWithTemplate("Password should contain upper case characters.").addConstraintViolation();
        }

        if(!s.matches(regexLowerCase)) {
            isValid = false;
            constraintValidatorContext.buildConstraintViolationWithTemplate("Password should contain lower case characters.").addConstraintViolation();
        }

        if(!s.matches(regexSpecialSymbol)) {
            isValid = false;
            constraintValidatorContext.buildConstraintViolationWithTemplate("Password should contain special symbols.").addConstraintViolation();
        }

        if(!s.matches(regexNumber)) {
            isValid = false;
            constraintValidatorContext.buildConstraintViolationWithTemplate("Password should contain digits.").addConstraintViolation();
        }

        if(!isValid) {
            constraintValidatorContext.disableDefaultConstraintViolation();
        }

        return isValid;
    }
}
