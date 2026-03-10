package ru.neoflex.calculator.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.neoflex.calculator.config.CalculatorProperties;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;

@Component
@RequiredArgsConstructor
public class CalculatorUtils {

    private final CalculatorProperties properties;

    public BigDecimal calculateMonthlyPayment(BigDecimal amount, BigDecimal rate, int term) {

        int divisor = properties
                .getInsurance()
                .getUtils()
                .getMonthlyDivisor();

        BigDecimal monthlyRate = rate.divide(
                new BigDecimal(divisor),
                10,
                RoundingMode.HALF_UP
        );

        BigDecimal onePlusR = BigDecimal.ONE.add(monthlyRate);
        BigDecimal pow = onePlusR.pow(term);

        BigDecimal numerator = monthlyRate.multiply(pow);
        BigDecimal denominator = pow.subtract(BigDecimal.ONE);

        BigDecimal result = amount.multiply(
                numerator.divide(denominator, 10, RoundingMode.HALF_UP)
        );

        return result.setScale(2, RoundingMode.HALF_UP);
    }

    public static int calculateAge(LocalDate birthdate) {
        return Period.between(birthdate, LocalDate.now()).getYears();
    }

}