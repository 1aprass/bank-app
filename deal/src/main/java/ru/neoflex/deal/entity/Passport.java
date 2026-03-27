package ru.neoflex.deal.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "passport")
@Setter
@Getter
public class Passport {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "passport_uuid", nullable = false)
    private UUID passportId;

    @Column(name = "series", nullable = false)
    private String series;

    @Column(name = "number", nullable = false)
    private String number;

    @Column(name = "issue_branch")
    private String issueBranch;

    @Column(name = "issue_date")
    private LocalDate issueDate;
}
