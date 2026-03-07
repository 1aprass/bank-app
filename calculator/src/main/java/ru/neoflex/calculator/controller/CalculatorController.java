package ru.neoflex.calculator.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.neoflex.calculator.dto.*;
import ru.neoflex.calculator.service.CalculatorService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/calculator")
@RequiredArgsConstructor
public class CalculatorController {
    private final CalculatorService calculatorService;

    @PostMapping("/offers")
    @Operation(summary = "Calculate possible offers")
    public ResponseEntity<OfferResponse> calculateOffers(@Valid @RequestBody LoanStatementRequestDto request){

        OfferResponse response = calculatorService.calculateOffers(request);
        return ResponseEntity.ok(response);

    }



    @PostMapping("/calc")
    @Operation(summary = "Полный расчет кредита")
    public ResponseEntity<CreditDto> calculateCredit(
            @Valid @RequestBody ScoringDataDto scoringData) {

        CreditDto credit = new CreditDto();
        credit.setAmount(scoringData.getAmount());
        credit.setTerm(scoringData.getTerm());
        credit.setMonthlyPayment(new BigDecimal("25000.00"));
        credit.setRate(new BigDecimal("12.5"));
        credit.setPsk(new BigDecimal("150000.00"));
        credit.setIsInsuranceEnabled(scoringData.getIsInsuranceEnabled());
        credit.setIsSalaryClient(scoringData.getIsSalaryClient());

        PaymentScheduleElementDto payment1 = new PaymentScheduleElementDto();
        payment1.setNumber(1);
        payment1.setDate(LocalDate.now().plusMonths(1));
        payment1.setTotalPayment(new BigDecimal("25000.00"));
        payment1.setInterestPayment(new BigDecimal("5000.00"));
        payment1.setDebtPayment(new BigDecimal("20000.00"));
        payment1.setRemainingDebt(new BigDecimal("480000.00"));

        PaymentScheduleElementDto payment2 = new PaymentScheduleElementDto();
        payment2.setNumber(2);
        payment2.setDate(LocalDate.now().plusMonths(2));
        payment2.setTotalPayment(new BigDecimal("25000.00"));
        payment2.setInterestPayment(new BigDecimal("4800.00"));
        payment2.setDebtPayment(new BigDecimal("20200.00"));
        payment2.setRemainingDebt(new BigDecimal("459800.00"));

        credit.setPaymentSchedule(Arrays.asList(payment1, payment2));

        return ResponseEntity.ok(credit);
    }

}

