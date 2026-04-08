package ru.neoflex.statement.dto;


import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoanStatementRequestDto {
    @NotNull(message = "amount is required")
    @DecimalMin(value = "20000.0", message = "amount must be 20.000 or more")
    private BigDecimal amount;

    @Min(value = 6, message = "term must be 6 or more")
    @NotNull(message = "term is required")
    private Integer term;

    @NotBlank(message = "firstName is required")
    @Pattern(
            regexp = "^[a-zA-Z]{2,30}$",
            message = "first name must be from 2 to 30 characters (only letters)"
    )
    private String firstName;

    @Pattern(
            regexp = "^[a-zA-Z]{2,30}$",
            message = "last name must be from 2 to 30 characters"
    )
    @NotBlank(message = "lastName is required")
    private String lastName;

    @Pattern(
            regexp = "^$|^[a-zA-Z]{2,30}$",
            message = "Middle name must be empty or contains 2-30 letters"
    )
    private String middleName;

    @Email(message = "email must be valid")
    @NotBlank(message = "email is required")
    private String email;

    @NotNull(message = "birthdate is required")
    @Past(message = "birth date must be in the past")
    private LocalDate birthdate;

    @NotBlank(message = "passportSeries is required")
    @Pattern(
            regexp = "^\\d{4}$",
            message = "Must be 4 digits"
    )
    private String passportSeries;

    @NotBlank(message = "passportNumber is required")
    @Pattern(
            regexp = "^\\d{6}$",
            message = "Must be 6 digits"
    )
    private String passportNumber;
}

