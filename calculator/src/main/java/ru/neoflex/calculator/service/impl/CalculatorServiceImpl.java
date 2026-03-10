package ru.neoflex.calculator.service.impl;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import ru.neoflex.calculator.config.CalculatorProperties;
import ru.neoflex.calculator.dto.*;
import ru.neoflex.calculator.enums.Gender;
import ru.neoflex.calculator.service.CalculatorService;
import ru.neoflex.calculator.util.CalculatorUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
@RequiredArgsConstructor
@Service
public class CalculatorServiceImpl implements CalculatorService {

    private final CalculatorProperties properties;
    private final CalculatorUtils calculatorUtils;


    @Override
    public List<LoanOfferDto> calculateOffers(LoanStatementRequestDto request){


        List<LoanOfferDto> offers = generateOffers(request);
        offers.sort(Comparator.comparing(LoanOfferDto::getRate));
        return offers;

    }

    public List<LoanOfferDto> generateOffers(LoanStatementRequestDto request){
        List<LoanOfferDto> offers = new ArrayList<>();
        boolean[] insuranceValues = {false, true};
        boolean[] salaryValues = {false, true};

        for (boolean isInsuranceValues:insuranceValues){
            for (boolean isSalaryValues:salaryValues){
                LoanOfferDto offer = createOffer(request, isInsuranceValues, isSalaryValues);
                offers.add(offer);
            }
        }
        return offers;
    }


    @Override
    public CreditDto calculateCredit(ScoringDataDto scoringData) {

        validateScoringData(scoringData);

        BigDecimal rate = calculateRate(scoringData);


        BigDecimal monthlyPayment = calculatorUtils.calculateMonthlyPayment(
                scoringData.getAmount(),
                rate,
                scoringData.getTerm()
        );

        BigDecimal psk = calculatePsk(
                monthlyPayment,
                scoringData.getTerm(),
                scoringData.getAmount()
        );


        List<PaymentScheduleElementDto> paymentSchedule = generatePaymentSchedule(
                scoringData.getAmount(),
                rate,
                scoringData.getTerm(),
                monthlyPayment
        );

        CreditDto credit = new CreditDto();
        credit.setAmount(scoringData.getAmount());
        credit.setTerm(scoringData.getTerm());
        credit.setMonthlyPayment(monthlyPayment);
        credit.setRate(rate);
        credit.setPsk(psk);
        credit.setIsInsuranceEnabled(scoringData.getIsInsuranceEnabled());
        credit.setIsSalaryClient(scoringData.getIsSalaryClient());
        credit.setPaymentSchedule(paymentSchedule);

        return credit;

    }

    private void validateScoringData(ScoringDataDto scoringData) {

        int age = CalculatorUtils.calculateAge(scoringData.getBirthdate());

        if (age < properties.getScoring().getAge().getMin() || age > properties.getScoring().getAge().getMax()) {
            throw new IllegalArgumentException("Age must be between " + properties.getScoring().getAge().getMin() +
                    " and " + properties.getScoring().getAge().getMax() + " years");
        }

        EmploymentDto emp = scoringData.getEmployment();

        if (emp.getWorkExperienceTotal() < properties.getScoring().getExperience().getTotalMin()) {
            throw new IllegalArgumentException("Total work experience must be at least " +
                    properties.getScoring().getExperience().getTotalMin() + " months");
        }

        if (emp.getWorkExperienceCurrent() < properties.getScoring().getExperience().getCurrentMin()) {
            throw new IllegalArgumentException("Current work experience must be at least " +
                    properties.getScoring().getExperience().getCurrentMin() + " months");
        }

        BigDecimal maxAllowedAmount = emp.getSalary().multiply(properties.getScoring().getSalaryMultiplier());

        if (scoringData.getAmount().compareTo(maxAllowedAmount) > 0) {
            throw new IllegalArgumentException("Loan amount exceeds " +
                    properties.getScoring().getSalaryMultiplier() + " times your salary");
        }
    }

    private BigDecimal calculateRate(ScoringDataDto scoringData) {

        BigDecimal rate = BigDecimal.valueOf(properties.getBaseRate());

        EmploymentDto emp = scoringData.getEmployment();
        int age = CalculatorUtils.calculateAge(scoringData.getBirthdate());

        if (emp.getEmploymentStatus() != null) {
            switch (emp.getEmploymentStatus()) {
                case UNEMPLOYED:
                    throw new IllegalArgumentException("Unemployed clients are not eligible");
                case SELF_EMPLOYED:
                    rate = rate.add(properties.getScoring().getRateAdjustments().getSelfEmployed());
                    break;
                case BUSINESS_OWNER:
                    rate = rate.add(properties.getScoring().getRateAdjustments().getBusinessOwner());
                    break;
            }
        }

        if (emp.getPosition() != null) {
            switch (emp.getPosition()) {
                case MID_MANAGER:
                    rate = rate.subtract(properties.getScoring().getRateAdjustments().getMidManager());
                    break;
                case TOP_MANAGER:
                    rate = rate.subtract(properties.getScoring().getRateAdjustments().getTopManager());
                    break;
            }
        }

        if (scoringData.getMaritalStatus() != null) {
            switch (scoringData.getMaritalStatus()) {
                case MARRIED:
                    rate = rate.subtract(properties.getScoring().getRateAdjustments().getMarried());
                    break;
                case DIVORCED:
                    rate = rate.add(properties.getScoring().getRateAdjustments().getDivorced());
                    break;
            }
        }

        if (scoringData.getGender() != null) {
            if (scoringData.getGender() == Gender.FEMALE && age >= properties.getScoring().getRateAdjustments().getFemale().getAgeFrom()
                    && age <= properties.getScoring().getRateAdjustments().getFemale().getAgeTo()) {
                rate = rate.subtract(properties.getScoring().getRateAdjustments().getFemale().getAdjustment());
            } else if (scoringData.getGender() == Gender.MALE && age >= properties.getScoring().getRateAdjustments().getMale().getAgeFrom()
                    && age <= properties.getScoring().getRateAdjustments().getMale().getAgeTo()) {
                rate = rate.subtract(properties.getScoring().getRateAdjustments().getMale().getAdjustment());
            } else if (scoringData.getGender() == Gender.NON_BINARY) {
                rate = rate.add(properties.getScoring().getRateAdjustments().getNonBinary());
            }
        }

        if (scoringData.getIsSalaryClient()) {
            rate = rate.subtract(BigDecimal.valueOf(properties.getSalaryClientBonus()));
        }

        if (scoringData.getIsInsuranceEnabled()) {
            rate = rate.subtract(BigDecimal.valueOf(properties.getInsuranceDiscount()));
        }

        return rate;
    }

    private BigDecimal calculatePsk(BigDecimal monthlyPayment, int term, BigDecimal amount) {

        return monthlyPayment
                .multiply(BigDecimal.valueOf(term))
                .subtract(amount)
                .setScale(2, RoundingMode.HALF_UP);
    }


    private List<PaymentScheduleElementDto> generatePaymentSchedule(
            BigDecimal amount,
            BigDecimal rate,
            int term,
            BigDecimal monthlyPayment) {

        List<PaymentScheduleElementDto> schedule = new ArrayList<>();
        BigDecimal remainingDebt = amount;

        BigDecimal monthlyRate = rate.divide(
                new BigDecimal(properties.getInsurance().getUtils().getMonthlyDivisor()),
                10,
                RoundingMode.HALF_UP
        );

        LocalDate currentDate = LocalDate.now().plusMonths(1);

        for (int i = 1; i <= term; i++) {
            PaymentScheduleElementDto element = new PaymentScheduleElementDto();
            element.setNumber(i);
            element.setDate(currentDate.plusMonths(i - 1));

            BigDecimal interestPayment = remainingDebt.multiply(monthlyRate)
                    .setScale(2, RoundingMode.HALF_UP);

            BigDecimal debtPayment = monthlyPayment.subtract(interestPayment)
                    .setScale(2, RoundingMode.HALF_UP);

            remainingDebt = remainingDebt.subtract(debtPayment)
                    .setScale(2, RoundingMode.HALF_UP);

            if (remainingDebt.compareTo(BigDecimal.ZERO) < 0) {
                remainingDebt = BigDecimal.ZERO;
            }

            element.setTotalPayment(monthlyPayment);
            element.setInterestPayment(interestPayment);
            element.setDebtPayment(debtPayment);
            element.setRemainingDebt(remainingDebt);

            schedule.add(element);
        }

        return schedule;
    }

    private LoanOfferDto createOffer(LoanStatementRequestDto request, boolean isInsuranceEnabled, boolean isSalaryClient) {
        BigDecimal rate = BigDecimal.valueOf(properties.getBaseRate());

        if (!isSalaryClient) {
            rate = rate.add(BigDecimal.valueOf(properties.getSalaryClientBonus()));
        }
        if (isInsuranceEnabled) {
            rate = rate.subtract(BigDecimal.valueOf(properties.getInsuranceDiscount()));
        }

        BigDecimal totalAmount = request.getAmount();

        CalculatorProperties.Threshold t = properties.getInsurance().getThresholds();
        if (totalAmount.compareTo(t.getMaxS()) <= 0) totalAmount = totalAmount.add(t.getFeeS());
        else if (totalAmount.compareTo(t.getMaxM()) <= 0) totalAmount = totalAmount.add(t.getFeeM());
        else if (totalAmount.compareTo(t.getMaxL()) <= 0) totalAmount = totalAmount.add(t.getFeeL());
        else totalAmount = totalAmount.add(t.getFeeXl());

        BigDecimal monthlyPayment = calculatorUtils.calculateMonthlyPayment(
                totalAmount, rate, request.getTerm()
        );

        LoanOfferDto offer = new LoanOfferDto();
        offer.setStatementId(null);
        offer.setRequestedAmount(request.getAmount());
        offer.setTotalAmount(totalAmount);
        offer.setTerm(request.getTerm());
        offer.setMonthlyPayment(monthlyPayment);
        offer.setRate(rate);
        offer.setIsInsuranceEnabled(isInsuranceEnabled);
        offer.setIsSalaryClient(isSalaryClient);

        return offer;
    }

}
