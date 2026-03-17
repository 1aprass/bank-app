package ru.neoflex.calculator.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.neoflex.calculator.config.CalculatorProperties;
import ru.neoflex.calculator.dto.PaymentScheduleElementDto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CalculatorUtils {

    private final CalculatorProperties properties;

    public BigDecimal calculateMonthlyPayment(BigDecimal amount, BigDecimal rate, int term) {
        log.debug("Input. CalculatorUtils calculateMonthlyPayment() - Расчет ежемесячного платежа: term={}", term);
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
        BigDecimal payment = result.setScale(2, RoundingMode.HALF_UP);
        log.debug("Output. CalculatorUtils calculateMonthlyPayment() - Ежемесячный платеж рассчитан: {}", payment);
        return payment;
    }

    public static int calculateAge(LocalDate birthdate) {
        log.debug("Input. CalculatorUtils calculateAge() - Расчет возраста");
        int age = Period.between(birthdate, LocalDate.now()).getYears();
        log.debug("Output. CalculatorUtils calculateAge() - Возраст рассчитан: {}", age);
        return age;
    }

    public BigDecimal calculatePsk(BigDecimal monthlyPayment, int term, BigDecimal amount) {
        log.debug("Input. CalculatorUtils calculatePsk() - Расчет ПСК: term={}", term);

        List<PaymentScheduleElementDto> schedule = new ArrayList<>();
        LocalDate currentDate = LocalDate.now().plusMonths(1);

        for (int i = 1; i <= term; i++) {
            PaymentScheduleElementDto payment = new PaymentScheduleElementDto();
            payment.setNumber(i);
            payment.setDate(currentDate.plusMonths(i - 1));
            payment.setTotalPayment(monthlyPayment);
            schedule.add(payment);
        }

        BigDecimal psk = calculatePskByIteration(amount, schedule);

        log.debug("Output. CalculatorUtils calculatePsk() - ПСК рассчитан: {}%", psk);
        return psk;
    }

    private BigDecimal calculatePskByIteration(BigDecimal amount, List<PaymentScheduleElementDto> schedule) {
        log.debug("Input. CalculatorUtils calculatePskByIteration() - Итерационный расчет ПСК");
        BigDecimal low = BigDecimal.ZERO;
        BigDecimal high = BigDecimal.valueOf(1000);
        BigDecimal tolerance = new BigDecimal("0.0001");
        LocalDate issueDate = LocalDate.now();

        for (int i = 0; i < 100; i++) {
            BigDecimal mid = low.add(high)
                    .divide(BigDecimal.valueOf(2), 10, RoundingMode.HALF_UP);

            BigDecimal npv = calculateNpv(amount, schedule, issueDate, mid);

            if (npv.abs().compareTo(tolerance) < 0) {
                log.debug("Output. CalculatorUtils calculatePskByIteration() - ПСК найден на итерации {}: {}", i, mid);
                return mid.setScale(2, RoundingMode.HALF_UP);
            }

            if (npv.compareTo(BigDecimal.ZERO) > 0) {
                low = mid;
            } else {
                high = mid;
            }
        }

        BigDecimal result = low.add(high)
                .divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);

        log.debug("Output. CalculatorUtils calculatePskByIteration() - ПСК рассчитан (макс итераций): {}", result);
        return result;
    }

    private BigDecimal calculateNpv(BigDecimal amount,
                                    List<PaymentScheduleElementDto> schedule,
                                    LocalDate issueDate,
                                    BigDecimal ratePercent) {
        log.debug("Input. CalculatorUtils calculateNpv() - Расчет NPV");
        BigDecimal npv = amount.negate();

        for (PaymentScheduleElementDto payment : schedule) {
            long days = ChronoUnit.DAYS.between(issueDate, payment.getDate());

            if (days <= 0) continue;

            BigDecimal dailyRate = ratePercent
                    .divide(BigDecimal.valueOf(properties.getInsurance().getUtils().getPercentConversion()),
                            10, RoundingMode.HALF_UP)
                    .divide(BigDecimal.valueOf(properties.getInsurance().getUtils().getDaysInYear()),
                            10, RoundingMode.HALF_UP);

            BigDecimal discountFactor = BigDecimal.ONE;
            for (int j = 0; j < days; j++) {
                discountFactor = discountFactor
                        .multiply(BigDecimal.ONE.add(dailyRate));
            }

            BigDecimal discountedPayment = payment.getTotalPayment()
                    .divide(discountFactor, 10, RoundingMode.HALF_UP);

            npv = npv.add(discountedPayment);
        }
        log.debug("Output. CalculatorUtils calculateNpv() - NPV рассчитан: {}", npv);
        return npv;
    }

}