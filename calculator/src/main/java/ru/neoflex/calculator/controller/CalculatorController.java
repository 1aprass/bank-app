package ru.neoflex.calculator.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    @PostMapping("/offers")
    @Operation(summary = "Calculate possible offers")
    public ResponseEntity<List<LoanOfferDto>> calculateOffers(@Valid @RequestBody LoanStatementRequestDto request){
        List<LoanOfferDto> response = calculatorService.calculateOffers(request);

        return ResponseEntity.ok(response);

    }

    @PostMapping("/calc")
    @Operation(summary = "Полный расчет кредита")
    public ResponseEntity<CreditDto> calculateCredit( @Valid @RequestBody ScoringDataDto scoringData) {

        CreditDto credit = calculatorService.calculateCredit(scoringData);

        return ResponseEntity.ok(credit);
    }

}

