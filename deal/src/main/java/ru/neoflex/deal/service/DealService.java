package ru.neoflex.deal.service;

import ru.neoflex.deal.dto.FinishRegistrationRequestDto;
import ru.neoflex.deal.dto.LoanOfferDto;
import ru.neoflex.deal.dto.LoanStatementRequestDto;

import java.util.List;

public interface DealService {
    List<LoanOfferDto> createStatement(LoanStatementRequestDto request);
    void selectOffer(LoanOfferDto request);
    void finishRegistration(FinishRegistrationRequestDto requestDto, String statementId);
}
