package ru.neoflex.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.neoflex.gateway.enums.ApplicationStatus;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StatementDto {
    private String id;
    private ClientDto client;
    private CreditResponseDto credit;
    private ApplicationStatus applicationStatus;
    private LocalDateTime creationDate;
    private LoanOfferDto appliedOffer;
    private LocalDateTime signDate;
}
