package ru.neoflex.deal.dto;


import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.neoflex.deal.enums.EmploymentPosition;
import ru.neoflex.deal.enums.EmploymentStatus;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmploymentDto {

    @NotNull(message = "Employment status is required")
    private EmploymentStatus employmentStatus;

    @Pattern(
            regexp = "^\\d{10}$|^\\d{12}$",
            message = "Employer INN must be 10 digits (legal entity) or 12 digits (individual entrepreneur)"
    )
    private String employerINN;

    @Positive(message = "Salary must be positive")
    @Digits(integer = 8, fraction = 2, message = "Salary must have valid format (up to 8 integer and 2 fraction digits)")
    private BigDecimal salary;

    private EmploymentPosition position;

    @NotNull(message = "Total work experience is required")
    @Min(value = 0, message = "Total work experience cannot be negative")
    @Max(value = 600, message = "Total work experience cannot exceed 600 months")
    private Integer workExperienceTotal;

    @NotNull(message = "Current work experience is required")
    @Min(value = 0, message = "Current work experience cannot be negative")
    @Max(value = 600, message = "Current work experience cannot exceed 600 months")
    private Integer workExperienceCurrent;
}