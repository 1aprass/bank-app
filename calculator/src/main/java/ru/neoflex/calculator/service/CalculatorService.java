package ru.neoflex.calculator.service;

import ru.neoflex.calculator.dto.CreditDto;
import ru.neoflex.calculator.dto.LoanOfferDto;
import ru.neoflex.calculator.dto.LoanStatementRequestDto;
import ru.neoflex.calculator.dto.ScoringDataDto;

import java.util.List;

public interface CalculatorService {
    List<LoanOfferDto> calculateOffers(LoanStatementRequestDto request);
    CreditDto calculateCredit(ScoringDataDto scoringData);

}
