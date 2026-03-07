package ru.neoflex.calculator.service;

import ru.neoflex.calculator.dto.LoanOfferDto;
import ru.neoflex.calculator.dto.LoanStatementRequestDto;
import ru.neoflex.calculator.dto.OfferResponse;

import java.util.List;

public interface CalculatorService {
    OfferResponse calculateOffers(LoanStatementRequestDto request);

}
