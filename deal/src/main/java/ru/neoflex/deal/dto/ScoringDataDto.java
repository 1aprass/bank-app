package ru.neoflex.deal.dto;


import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.neoflex.deal.enums.Gender;
import ru.neoflex.deal.enums.MaritalStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScoringDataDto {

    @NotNull(message = "amount is required")
    @DecimalMin(value = "20000", message = "Amount must be at least 20000")
    private BigDecimal amount;

    @NotNull(message = "term is required")
    @Min(value = 6, message = "Term must be at least 6 months")
    private Integer term;

    @NotNull(message = "firstName is required")
    @Pattern(
            regexp = "^[a-zA-Z]{2,30}$",
            message = "First name must be empty or contains 2-30 letters"
    )
    private String firstName;

    @NotNull(message = "lastName is required")
    @Pattern(
            regexp = "^[a-zA-Z]{2,30}$",
            message = "Last name must be empty or contains 2-30 letters"
    )
    private String lastName;

    @Pattern(
            regexp = "^$|^[a-zA-Z]{2,30}$",
            message = "Middle name must be empty or contains 2-30 letters"
    )
    private String middleName;

    @NotNull(message = "gender is required")
    private Gender gender;

    @NotNull(message = "birthdate is required")
    @Past(message = "Birthdate must be in the past")
    private LocalDate birthdate;

    @NotNull(message = "amount is required")
    @Pattern(regexp = "^\\d{4}$", message = "Passport series must contain 4 digits")
    private String passportSeries;

    @NotNull(message = "passportNumber is required")
    @Pattern(regexp = "^\\d{6}$", message = "Passport number must contain 6 digits")
    private String passportNumber;

    @NotNull(message = "passportIssueDate is required")
    @Past(message = "Passport Issue Date must be in the past")
    private LocalDate passportIssueDate;

    @NotNull(message = "passportIssueBranch is required")
    private String passportIssueBranch;

    @NotNull(message = "maritalStatus is required")
    private MaritalStatus maritalStatus;

    @NotNull(message = "dependentAmount is required")
    private Integer dependentAmount;

    @Valid
    @NotNull(message = "employment is required")
    private EmploymentDto employment;

    @NotNull(message = "accountNumber is required")
    private String accountNumber;
    @NotNull(message = "isInsuranceEnabled is required")
    private Boolean isInsuranceEnabled;
    @NotNull(message = "isSalaryClient is required")
    private Boolean isSalaryClient;
}
