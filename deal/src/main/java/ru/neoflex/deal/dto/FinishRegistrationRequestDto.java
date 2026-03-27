package ru.neoflex.deal.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import ru.neoflex.deal.enums.Gender;
import ru.neoflex.deal.enums.MaritalStatus;

import java.time.LocalDate;

@Setter
@Getter
public class FinishRegistrationRequestDto {
    @NotNull(message = "gender is required")
    private Gender gender;

    @NotNull(message = "maritalStatus is required")
    private MaritalStatus maritalStatus;

    @NotNull
    @Positive(message = "dependentAmount must be positive")
    private Integer dependentAmount;

    @NotNull(message = "passport issue date is required")
    @Past(message = "passport issue date date must be in the past")
    private LocalDate passportIssueDate;

    @NotBlank(message = "passportIssueBranch is required")
    private String passportIssueBranch;

    @Valid
    @NotNull
    private EmploymentDto employmentDto;

    @NotBlank(message = "accountNumber is required")
    private String accountNumber;
}
