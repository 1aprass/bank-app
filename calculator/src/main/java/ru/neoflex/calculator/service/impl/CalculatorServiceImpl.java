package ru.neoflex.calculator.service.impl;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import ru.neoflex.calculator.config.CalculatorProperties;
import ru.neoflex.calculator.dto.*;
import ru.neoflex.calculator.enums.EmploymentStatus;
import ru.neoflex.calculator.enums.Gender;
import ru.neoflex.calculator.service.CalculatorService;
import ru.neoflex.calculator.util.CalculatorUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class CalculatorServiceImpl implements CalculatorService {

    private final CalculatorProperties properties;
    private final CalculatorUtils calculatorUtils;


    @Override
    public List<LoanOfferDto> calculateOffers(LoanStatementRequestDto request){

        log.debug("Input. CalculatorServiceImpl calculateOffers() - Начало расчета предложений");
        prescoring(request);
        List<LoanOfferDto> offers = generateOffers(request);
        log.debug("Output. CalculatorServiceImpl calculateOffers() - Сгенерировано {} предложений", offers.size());
        offers.sort(Comparator.comparing(LoanOfferDto::getRate).reversed());
        log.debug("Output. CalculatorServiceImpl calculateOffers() - Возвращено {} отсортированных предложений",
                offers.size());
        return offers;

    }
    public void prescoring(LoanStatementRequestDto request){
        log.debug("Input. CalculatorServiceImpl prescoring() - Проверка возраста клиента");
        if (request.getBirthdate() != null) {
            int age = CalculatorUtils.calculateAge(request.getBirthdate());
            if (age < 18) {
                log.error("Error. CalculatorServiceImpl prescoring() - Клиент младше 18 лет: возраст {}", age);
                throw new IllegalArgumentException("Client must be at least 18 years old");
            }
            log.debug("Output. CalculatorServiceImpl prescoring() - Возраст клиента {} лет - OK", age);
        } else {
            log.error("Error. CalculatorServiceImpl prescoring() - Дата рождения не указана");
            throw new IllegalArgumentException("Birthdate is required");
        }
    }

    public List<LoanOfferDto> generateOffers(LoanStatementRequestDto request){
        log.debug("Input. CalculatorServiceImpl generateOffers() - Генерация комбинаций предложений");
        List<LoanOfferDto> offers = new ArrayList<>();
        boolean[] insuranceValues = {false, true};
        boolean[] salaryValues = {false, true};

        for (boolean isInsuranceValues:insuranceValues){
            for (boolean isSalaryValues:salaryValues){
                LoanOfferDto offer = createOffer(request, isInsuranceValues, isSalaryValues);
                log.debug("Output. CalculatorServiceImpl generateOffers() - Сгенерировано предложение: insurance={}, salaryClient={}, rate={}",
                        isInsuranceValues, isSalaryValues, offer.getRate());
                offers.add(offer);
            }
        }
        log.debug("Output. CalculatorServiceImpl generateOffers() - Всего сгенерировано {} предложений",
                offers.size());
        return offers;
    }


    private BigDecimal calculateTotalAmountWithInsurance(BigDecimal baseAmount, boolean isInsuranceEnabled) {
        log.debug("Input. CalculatorServiceImpl calculateTotalAmountWithInsurance() - Расчет суммы со страховкой");
        if (!isInsuranceEnabled) {
            log.debug("Output. CalculatorServiceImpl calculateTotalAmountWithInsurance() - Страховка не включена, сумма = {}",
                    baseAmount);
            return baseAmount;
        }

        CalculatorProperties.Threshold t = properties.getInsurance().getThresholds();
        BigDecimal totalAmount;

        if (baseAmount.compareTo(t.getMaxS()) <= 0) totalAmount = baseAmount.add(t.getFeeS());
        else if (baseAmount.compareTo(t.getMaxM()) <= 0) totalAmount = baseAmount.add(t.getFeeM());
        else if (baseAmount.compareTo(t.getMaxL()) <= 0) totalAmount = baseAmount.add(t.getFeeL());
        else totalAmount = baseAmount.add(t.getFeeXl());

        log.debug("Output. CalculatorServiceImpl calculateTotalAmountWithInsurance() - Страховка включена, итоговая сумма = {}", totalAmount);
        return totalAmount;
    }

    @Override
    public CreditDto calculateCredit(ScoringDataDto scoringData) {

        log.debug("Input. CalculatorServiceImpl calculateCredit() - Начало полного расчета кредита");
        validateScoringData(scoringData);

        BigDecimal rate = calculateRate(scoringData);
        log.debug("Output. CalculatorServiceImpl calculateCredit() - Рассчитана процентная ставка: {}", rate);

        BigDecimal amountForCalculation = calculateTotalAmountWithInsurance(scoringData.getAmount(),scoringData.getIsInsuranceEnabled());

        BigDecimal monthlyPayment = calculatorUtils.calculateMonthlyPayment(
                amountForCalculation,
                rate,
                scoringData.getTerm()
        );
        log.debug("Output. CalculatorServiceImpl calculateCredit() - Рассчитан ежемесячный платеж: {}",
                monthlyPayment);

        BigDecimal psk = calculatorUtils.calculatePsk(
                monthlyPayment,
                scoringData.getTerm(),
                scoringData.getAmount()
        );
        log.debug("Output. CalculatorServiceImpl calculateCredit() - Рассчитан ПСК: {}", psk);

        List<PaymentScheduleElementDto> paymentSchedule = generatePaymentSchedule(
                amountForCalculation,
                rate,
                scoringData.getTerm(),
                monthlyPayment
        );

        log.debug("Output. CalculatorServiceImpl calculateCredit() - Сгенерирован график платежей из {} элементов",
                paymentSchedule.size());
        CreditDto credit = new CreditDto();
        credit.setAmount(amountForCalculation);
        credit.setTerm(scoringData.getTerm());
        credit.setMonthlyPayment(monthlyPayment);
        credit.setRate(rate);
        credit.setPsk(psk);
        credit.setIsInsuranceEnabled(scoringData.getIsInsuranceEnabled());
        credit.setIsSalaryClient(scoringData.getIsSalaryClient());
        credit.setPaymentSchedule(paymentSchedule);

        log.debug("Output. CalculatorServiceImpl calculateCredit() - Расчет кредита завершен");
        return credit;

    }

    private void validateScoringData(ScoringDataDto scoringData) {
        log.debug("Input. CalculatorServiceImpl validateScoringData() - Валидация данных для скоринга");
        if (scoringData.getEmployment().getEmploymentStatus() == EmploymentStatus.UNEMPLOYED){
            log.error("Error. CalculatorServiceImpl validateScoringData() - Клиент безработный");
            throw new IllegalArgumentException("Unemployed clients are not eligible");
        }

        int age = CalculatorUtils.calculateAge(scoringData.getBirthdate());
        log.debug("Output. CalculatorServiceImpl validateScoringData() - Возраст клиента: {}", age);
        if (age < properties.getScoring().getAge().getMin() || age > properties.getScoring().getAge().getMax()) {
            log.error("Error. CalculatorServiceImpl validateScoringData() - Возраст {} вне допустимого диапазона {}-{}",
                    age, properties.getScoring().getAge().getMin(), properties.getScoring().getAge().getMax());

            throw new IllegalArgumentException("Age must be between " + properties.getScoring().getAge().getMin() +
                    " and " + properties.getScoring().getAge().getMax() + " years");
        }

        EmploymentDto emp = scoringData.getEmployment();

        if (emp.getWorkExperienceTotal() < properties.getScoring().getExperience().getTotalMin()) {
            log.error("Error. CalculatorServiceImpl validateScoringData() - Общий стаж {} меньше требуемого {}",
                    emp.getWorkExperienceTotal(), properties.getScoring().getExperience().getTotalMin());

            throw new IllegalArgumentException("Total work experience must be at least " +
                    properties.getScoring().getExperience().getTotalMin() + " months");
        }

        if (emp.getWorkExperienceCurrent() < properties.getScoring().getExperience().getCurrentMin()) {
            log.error("Error. CalculatorServiceImpl validateScoringData() - Текущий стаж {} меньше требуемого {}",
                    emp.getWorkExperienceCurrent(), properties.getScoring().getExperience().getCurrentMin());

            throw new IllegalArgumentException("Current work experience must be at least " +
                    properties.getScoring().getExperience().getCurrentMin() + " months");
        }

        BigDecimal maxAllowedAmount = emp.getSalary().multiply(properties.getScoring().getSalaryMultiplier());

        if (scoringData.getAmount().compareTo(maxAllowedAmount) > 0) {
            log.error("Error. CalculatorServiceImpl validateScoringData() - Запрошенная сумма {} превышает допустимую {}",
                    scoringData.getAmount(), maxAllowedAmount);

            throw new IllegalArgumentException("Loan amount exceeds " +
                    properties.getScoring().getSalaryMultiplier() + " times your salary");
        }

        log.debug("Output. CalculatorServiceImpl validateScoringData() - Валидация успешно пройдена");
    }

    private BigDecimal calculateRate(ScoringDataDto scoringData) {

        log.debug("Input. CalculatorServiceImpl calculateRate() - Расчет процентной ставки");
        BigDecimal rate = BigDecimal.valueOf(properties.getBaseRate());
        log.debug("Output. CalculatorServiceImpl calculateRate() - Базовая ставка: {}", rate);
        EmploymentDto emp = scoringData.getEmployment();
        int age = CalculatorUtils.calculateAge(scoringData.getBirthdate());

        if (emp.getEmploymentStatus() != null) {
            switch (emp.getEmploymentStatus()) {
                case SELF_EMPLOYED:
                    rate = rate.add(properties.getScoring().getRateAdjustments().getSelfEmployed());
                    log.debug("Output. CalculatorServiceImpl calculateRate() - Применена надбавка для самозанятого. Новая ставка: {}", rate);
                    break;
                case BUSINESS_OWNER:
                    rate = rate.add(properties.getScoring().getRateAdjustments().getBusinessOwner());
                    log.debug("Output. CalculatorServiceImpl calculateRate() - Применена надбавка для владельца бизнеса. Новая ставка: {}", rate);
                    break;
            }
        }

        if (emp.getPosition() != null) {
            switch (emp.getPosition()) {
                case MID_MANAGER:
                    rate = rate.add(properties.getScoring().getRateAdjustments().getMidManager());
                    log.debug("Output. CalculatorServiceImpl calculateRate() - Применена надбавка для мидл-менеджера. Новая ставка: {}", rate);
                    break;
                case TOP_MANAGER:
                    rate = rate.add(properties.getScoring().getRateAdjustments().getTopManager());
                    log.debug("Output. CalculatorServiceImpl calculateRate() - Применена надбавка для топ-менеджера. Новая ставка: {}", rate);
                    break;
            }
        }

        if (scoringData.getMaritalStatus() != null) {
            switch (scoringData.getMaritalStatus()) {
                case MARRIED:
                    rate = rate.add(properties.getScoring().getRateAdjustments().getMarried());
                    log.debug("Output. CalculatorServiceImpl calculateRate() - Применена надбавка для женатого. Новая ставка: {}", rate);
                    break;
                case DIVORCED:
                    rate = rate.add(properties.getScoring().getRateAdjustments().getDivorced());
                    log.debug("Output. CalculatorServiceImpl calculateRate() - Применена надбавка для разведенного. Новая ставка: {}", rate);
                    break;
            }
        }

        if (scoringData.getGender() != null) {

            if (scoringData.getGender() == Gender.FEMALE && age >= properties.getScoring().getRateAdjustments().getFemale().getAgeFrom()
                    && age <= properties.getScoring().getRateAdjustments().getFemale().getAgeTo()) {
                rate = rate.add(properties.getScoring().getRateAdjustments().getFemale().getAdjustment());
                log.debug("Output. CalculatorServiceImpl calculateRate() - Применена надбавка для женщины. Новая ставка: {}", rate);
            } else if (scoringData.getGender() == Gender.MALE && age >= properties.getScoring().getRateAdjustments().getMale().getAgeFrom()
                    && age <= properties.getScoring().getRateAdjustments().getMale().getAgeTo()) {
                rate = rate.add(properties.getScoring().getRateAdjustments().getMale().getAdjustment());
                log.debug("Output. CalculatorServiceImpl calculateRate() - Применена надбавка для мужчины. Новая ставка: {}", rate);
            } else if (scoringData.getGender() == Gender.NON_BINARY) {
                rate = rate.add(properties.getScoring().getRateAdjustments().getNonBinary());
                log.debug("Output. CalculatorServiceImpl calculateRate() - Применена надбавка для небинарного лица. Новая ставка: {}", rate);
            } else {
                log.debug("Output. CalculatorServiceImpl calculateRate() - Пол не был распознан");
            }
        }

        if (scoringData.getIsInsuranceEnabled()) {
            rate = rate.subtract(BigDecimal.valueOf(properties.getInsuranceDiscount()));
            log.debug("Output. CalculatorServiceImpl calculateRate() - Применена скидка за страховку. Новая ставка: {}", rate);
        }
        log.debug("Output. CalculatorServiceImpl calculateRate() - Итоговая процентная ставка: {}", rate);
        return rate;
    }




    private List<PaymentScheduleElementDto> generatePaymentSchedule(
            BigDecimal amount,
            BigDecimal rate,
            int term,
            BigDecimal monthlyPayment) {

        log.debug("Input. CalculatorServiceImpl generatePaymentSchedule() - Генерация графика платежей");
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

            BigDecimal debtPayment;
            BigDecimal totalPayment;
            if (i == term) {
                debtPayment = remainingDebt;
                totalPayment = interestPayment.add(debtPayment);
                remainingDebt = BigDecimal.ZERO;
            } else {
                debtPayment = monthlyPayment.subtract(interestPayment)
                        .setScale(2, RoundingMode.HALF_UP);
                remainingDebt = remainingDebt.subtract(debtPayment)
                        .setScale(2, RoundingMode.HALF_UP);
                totalPayment = monthlyPayment;
            }
            element.setTotalPayment(totalPayment);
            element.setInterestPayment(interestPayment);
            element.setDebtPayment(debtPayment);
            element.setRemainingDebt(remainingDebt);

            schedule.add(element);
        }
        log.debug("Output. CalculatorServiceImpl generatePaymentSchedule() - Сгенерировано {} платежей", schedule.size());
        return schedule;
    }

    private LoanOfferDto createOffer(LoanStatementRequestDto request, boolean isInsuranceEnabled, boolean isSalaryClient) {

        log.debug("Input. CalculatorServiceImpl createOffer() - Создание предложения: insurance={}, salaryClient={}",
                isInsuranceEnabled, isSalaryClient);
        BigDecimal rate = BigDecimal.valueOf(properties.getBaseRate());

        if (!isSalaryClient) {
            rate = rate.add(BigDecimal.valueOf(properties.getSalaryClientBonus()));
        }
        if (isInsuranceEnabled) {
            rate = rate.subtract(BigDecimal.valueOf(properties.getInsuranceDiscount()));
        }

        BigDecimal totalAmount = calculateTotalAmountWithInsurance(request.getAmount(), isInsuranceEnabled);

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

        log.debug("Output. CalculatorServiceImpl createOffer() - Создано предложение: totalAmount={}, rate={}, monthlyPayment={}",
                totalAmount, rate, monthlyPayment);

        return offer;
    }

}
