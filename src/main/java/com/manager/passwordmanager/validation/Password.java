package com.manager.passwordmanager.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordValidator.class)
public @interface Password {

    String message() default "The password does not meet the requirements";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}

