package ru.neoflex.deal.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = EmploymentValidator.class)
public @interface ValidEmployment {
    String message() default "Invalid employment data";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
