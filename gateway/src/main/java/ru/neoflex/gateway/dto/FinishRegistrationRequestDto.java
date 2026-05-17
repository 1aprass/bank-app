package ru.neoflex.gateway.dto;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;
import ru.neoflex.gateway.enums.Gender;
import ru.neoflex.gateway.enums.MaritalStatus;

import java.time.LocalDate;

@Setter
@Getter
public class FinishRegistrationRequestDto {
    @NotNull(message = "gender is required")
    private Gender gender;

    @NotNull(message = "maritalStatus is required")
    private MaritalStatus maritalStatus;

    @NotNull
    @PositiveOrZero(message = "dependentAmount must be positive or zero")
    private Integer dependentAmount;

    @NotNull(message = "passport issue date is required")
    @Past(message = "passport issue date date must be in the past")
    private LocalDate passportIssueDate;

    @NotBlank(message = "passportIssueBranch is required")
    private String passportIssueBranch;

    @Valid
    @NotNull
    private EmploymentDto employment;

    @NotBlank(message = "accountNumber is required")
    private String accountNumber;
}

