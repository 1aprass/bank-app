package ru.neoflex.deal.validation;


import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.neoflex.deal.dto.EmploymentDto;
import ru.neoflex.deal.enums.EmploymentStatus;
import static ru.neoflex.deal.enums.EmploymentStatus.UNEMPLOYED;

public class EmploymentValidator implements ConstraintValidator<ValidEmployment, EmploymentDto> {
    @Override
    public boolean isValid(EmploymentDto dto, ConstraintValidatorContext context){

        context.disableDefaultConstraintViolation();
        if (dto == null) return true;

        EmploymentStatus status = dto.getEmploymentStatus();

        if(status == null){
            context.buildConstraintViolationWithTemplate("Employment status is required")
                    .addConstraintViolation();
            return false;
        }
        if(status == UNEMPLOYED) return true;

        if(dto.getEmployerINN() == null || dto.getEmployerINN().isBlank()) {
            context.buildConstraintViolationWithTemplate("Employer INN is required for employed clients")
                    .addConstraintViolation();

            return false;
        }
        if(dto.getSalary() == null || dto.getPosition() == null){
            context.buildConstraintViolationWithTemplate("Salary must be specified")
                    .addConstraintViolation();
            return false;
        }

        if(dto.getWorkExperienceTotal() != null && dto.getWorkExperienceCurrent() != null
                && dto.getWorkExperienceCurrent() > dto.getWorkExperienceTotal()){
            context.buildConstraintViolationWithTemplate("Current work experience cannot exceed total experience")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}
