package io.github.maradroman.waypointapi.plannedfund.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

public class FutureOrPresentDateValidator implements ConstraintValidator<FutureOrPresentDate, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true; // Let @NotBlank handle null/blank validation
        }

        try {
            LocalDate date = LocalDate.parse(value);
            LocalDate today = LocalDate.now();
            return !date.isBefore(today);
        } catch (Exception e) {
            return false; // Invalid date format
        }
    }
}
