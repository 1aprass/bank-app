package ru.neoflex.statement.dto;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoanOfferDto {

    @NotNull
    private UUID statementId;

    @NotNull
    @Positive(message = "requestedAmount must be positive")
    private BigDecimal requestedAmount;

    @NotNull
    @Positive(message = "totalAmount must be positive")
    private BigDecimal totalAmount;

    @Min(value = 6, message = "term must be 6 or more")
    @NotNull(message = "term is required")
    private Integer term;

    @NotNull
    @Positive(message = "monthlyPayment must be positive")
    private BigDecimal monthlyPayment;

    @NotNull
    @Positive(message = "rate must be positive")
    private BigDecimal rate;

    @NotNull
    private Boolean isInsuranceEnabled;

    @NotNull
    private Boolean isSalaryClient;
}