package ru.neoflex.deal.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import ru.neoflex.deal.enums.EmploymentStatus;
import ru.neoflex.deal.enums.EmploymentPosition;

import java.math.BigDecimal;
import java.util.UUID;

@Setter
@Getter
@Entity(name = "employment")
public class Employment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "employment_uuid", nullable = false)
    private UUID employmentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private EmploymentStatus employmentStatus;

    @Column(name = "employer_inn", nullable = false)
    private String employerINN;

    @Column(name = "salary")
    private BigDecimal salary;

    @Column(name = "employment_position", nullable = false)
    @Enumerated(EnumType.STRING)
    private EmploymentPosition employmentPosition;

    @Column(name = "work_experience_total", nullable = false)
    private Integer workExperienceTotal;

    @Column(name = "work_experience_current", nullable = false)
    private Integer workExperienceCurrent;

}
