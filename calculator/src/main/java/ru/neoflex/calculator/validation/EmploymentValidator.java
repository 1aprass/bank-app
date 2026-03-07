package ru.neoflex.calculator.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.neoflex.calculator.dto.EmploymentDto;
import ru.neoflex.calculator.enums.EmploymentStatus;

import static ru.neoflex.calculator.enums.EmploymentStatus.UNEMPLOYED;

public class EmploymentValidator implements ConstraintValidator<ValidEmployment, EmploymentDto> {
    @Override
    public boolean isValid(EmploymentDto dto, ConstraintValidatorContext context){
        if (dto == null) return true;

        EmploymentStatus status = dto.getEmploymentStatus();

        if(status == UNEMPLOYED) return true;

        if(dto.getEmployerINN() == null || dto.getSalary() == null || dto.getPosition() == null){
            return false;
        }

        if(dto.getWorkExperienceTotal() != null && dto.getWorkExperienceCurrent() != null
            && dto.getWorkExperienceCurrent() > dto.getWorkExperienceTotal()) return false;

        return true;
    }
}
