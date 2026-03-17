package ru.neoflex.calculator.validation;


import jakarta.validation.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ru.neoflex.calculator.dto.EmploymentDto;
import ru.neoflex.calculator.enums.EmploymentStatus;
import ru.neoflex.calculator.enums.Position;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class EmploymentValidatorTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private EmploymentDto createValidEmployment() {
        return new EmploymentDto(
                EmploymentStatus.EMPLOYED,
                "1234567890",
                new BigDecimal("50000"),
                Position.WORKER,
                24,
                12
        );
    }
    @Test
    void validEmployment_ShouldPassValidation() {
        EmploymentDto dto = createValidEmployment();

        Set<ConstraintViolation<EmploymentDto>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    @Test
    void employmentStatusNull_ShouldFailValidation() {
        EmploymentDto dto = createValidEmployment();
        dto.setEmploymentStatus(null);

        Set<ConstraintViolation<EmploymentDto>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
    }

    @Test
    void employedWithoutInn_ShouldFailValidation() {
        EmploymentDto dto = createValidEmployment();
        dto.setEmployerINN(null);

        Set<ConstraintViolation<EmploymentDto>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
    }

    @Test
    void employedWithoutSalary_ShouldFailValidation() {
        EmploymentDto dto = createValidEmployment();
        dto.setSalary(null);

        Set<ConstraintViolation<EmploymentDto>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
    }

    @Test
    void currentExperienceGreaterThanTotal_ShouldFailValidation() {
        EmploymentDto dto = createValidEmployment();
        dto.setWorkExperienceTotal(12);
        dto.setWorkExperienceCurrent(24);

        Set<ConstraintViolation<EmploymentDto>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
    }

    @Test
    void unemployed_ShouldPassValidation() {
        EmploymentDto dto = new EmploymentDto();
        dto.setEmploymentStatus(EmploymentStatus.UNEMPLOYED);
        dto.setWorkExperienceTotal(24);
        dto.setWorkExperienceCurrent(12);

        Set<ConstraintViolation<EmploymentDto>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }


    @Test
    void employedWithoutPosition_ShouldFailValidation() {
        EmploymentDto dto = createValidEmployment();
        dto.setPosition(null);

        Set<ConstraintViolation<EmploymentDto>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().contains("Salary must be specified")));
    }

    @Test
    void employedWithoutSalaryAndPosition_ShouldFailValidation() {
        EmploymentDto dto = createValidEmployment();
        dto.setSalary(null);
        dto.setPosition(null);

        Set<ConstraintViolation<EmploymentDto>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
        assertEquals(1, violations.size());
    }

    @Test
    void isValid_ShouldReturnTrue_WhenDtoIsNull() {
        EmploymentValidator validator = new EmploymentValidator();

        ConstraintValidatorContext context = Mockito.mock(ConstraintValidatorContext.class);

        boolean result = validator.isValid(null, context);

        assertTrue(result);
    }
}