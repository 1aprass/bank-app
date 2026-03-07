package ru.neoflex.calculator.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.neoflex.calculator.enums.EmploymentStatus;
import ru.neoflex.calculator.enums.Position;
import ru.neoflex.calculator.validation.ValidEmployment;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ValidEmployment
public class EmploymentDto {

    @NotNull(message = "Employment status is required")
    private EmploymentStatus employmentStatus;

    //@NotBlank(message = "INN is required")
    @Pattern(
            regexp = "^\\d{10}$|^\\d{12}$",
            message = "Employer INN must be 10 digits (legal entity) or 12 digits (individual entrepreneur)"
    )
    private String employerINN;

    //@NotNull(message = "Salary is required")
    @DecimalMin(value = "0.0", message = "Salary cannot be negative")
    @Digits(integer = 8, fraction = 2, message = "Salary must have valid format (up to 8 integer and 2 fraction digits)")
    private BigDecimal salary;

    //@NotNull(message = "Position is required")
    private Position position;

    @NotNull(message = "Total work experience is required")
    @Min(value = 0, message = "Total work experience cannot be negative")
    @Max(value = 600, message = "Total work experience cannot exceed 600 months")
    private Integer workExperienceTotal;

    @NotNull(message = "Current work experience is required")
    @Min(value = 0, message = "Current work experience cannot be negative")
    @Max(value = 600, message = "Current work experience cannot exceed 600 months")
    private Integer workExperienceCurrent;
}