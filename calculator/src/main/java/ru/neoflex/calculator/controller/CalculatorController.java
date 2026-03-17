package ru.neoflex.calculator.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.neoflex.calculator.dto.*;
import ru.neoflex.calculator.service.CalculatorService;

import java.util.*;

@RestController
@RequestMapping("/calculator")
@RequiredArgsConstructor
public class CalculatorController {
    private final CalculatorService calculatorService;
    private static final Logger log = LoggerFactory.getLogger(CalculatorController.class);

    @PostMapping("/offers")
    @Operation(summary = "Calculate possible offers")
    public ResponseEntity<List<LoanOfferDto>> calculateOffers(@Valid @RequestBody LoanStatementRequestDto request){
        log.debug("Input. CalculatorController calculateOffers() - Получен запрос на /offers");
        List<LoanOfferDto> response = calculatorService.calculateOffers(request);
        log.debug("Output. CalculatorController calculateOffers() - Отправлено {} предложений", response.size());
        return ResponseEntity.ok(response);

    }

    @PostMapping("/calc")
    @Operation(summary = "Полный расчет кредита")
    public ResponseEntity<CreditDto> calculateCredit( @Valid @RequestBody ScoringDataDto scoringData) {

        log.debug("Input. CalculatorController calculateCredit() - Получен запрос на /calc");
        CreditDto credit = calculatorService.calculateCredit(scoringData);
        log.debug("Output. CalculatorController calculateCredit() - CreditDto рассчитан");
        return ResponseEntity.ok(credit);
    }

}

