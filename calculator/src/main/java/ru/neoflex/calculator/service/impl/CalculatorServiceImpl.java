package ru.neoflex.calculator.service.impl;

import org.springframework.stereotype.Service;
import ru.neoflex.calculator.dto.LoanOfferDto;
import ru.neoflex.calculator.dto.LoanStatementRequestDto;
import ru.neoflex.calculator.dto.OfferResponse;
import ru.neoflex.calculator.service.CalculatorService;
import ru.neoflex.calculator.util.CalculatorUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


@Service
public class CalculatorServiceImpl implements CalculatorService {

    @Override
    public OfferResponse calculateOffers(LoanStatementRequestDto request){
        String prescoring = validatePrescoring(request);
        if (prescoring != null){
            OfferResponse response = new OfferResponse();
            response.setRejectionReason(prescoring);
            response.setSuccess(false);
            response.setOffers(null);

            return response;
        }

        List<LoanOfferDto> offers = generateOffers(request);
        OfferResponse response = new OfferResponse();
        response.setRejectionReason(null);
        response.setSuccess(true);
        response.setOffers(offers);

        return response;

    }

    public List<LoanOfferDto> generateOffers(LoanStatementRequestDto request){
        List<LoanOfferDto> offers = new ArrayList<>();
        boolean[] insuranceValues = {false, true};
        boolean[] salaryValues = {false, true};

        for (boolean IsInsuranceValues:insuranceValues){
            for (boolean IsSalaryValues:salaryValues){
                LoanOfferDto offer = createOffer(request, IsInsuranceValues, IsSalaryValues);
                offers.add(offer);
            }
        }
        return offers;
    }

    public LoanOfferDto createOffer(LoanStatementRequestDto request, boolean IsInsuranceValues, boolean IsSalaryValues){
        BigDecimal rate = new BigDecimal("0");

        if(IsInsuranceValues && IsSalaryValues) rate = new BigDecimal("24");
        if(IsInsuranceValues && !IsSalaryValues) rate = new BigDecimal("52");
        if(!IsInsuranceValues && IsSalaryValues) rate = new BigDecimal("32");
        if(!IsInsuranceValues && !IsSalaryValues) rate = new BigDecimal("60");

        BigDecimal totalAmount = request.getAmount();

        if (IsInsuranceValues) {

            if (totalAmount.compareTo(new BigDecimal("1000000")) <= 0) {
                totalAmount = totalAmount.add(new BigDecimal("20000"));
            }
            else if (totalAmount.compareTo(new BigDecimal("1000000")) > 0 &&
                    totalAmount.compareTo(new BigDecimal("3000000")) <= 0) {
                totalAmount = totalAmount.add(new BigDecimal("80000"));
            }
            else if (totalAmount.compareTo(new BigDecimal("3000000")) > 0 &&
                    totalAmount.compareTo(new BigDecimal("5000000")) <= 0) {
                totalAmount = totalAmount.add(new BigDecimal("150000"));
            }
            else {
                totalAmount = totalAmount.add(new BigDecimal("250000"));
            }
        }

        BigDecimal monthlyPayment = CalculatorUtils.calculateMonthlyPayment(
                totalAmount, rate, request.getTerm()
        );

        LoanOfferDto offer = new LoanOfferDto();
        offer.setStatementId(null);
        offer.setRequestedAmount(request.getAmount());
        offer.setTotalAmount(totalAmount);
        offer.setTerm(request.getTerm());
        offer.setMonthlyPayment(monthlyPayment);
        offer.setRate(rate);
        offer.setIsInsuranceEnabled(IsInsuranceValues);
        offer.setIsSalaryClient(IsSalaryValues);

        return offer;

    }

    String validatePrescoring(LoanStatementRequestDto request) {
        if (!request.getFirstName().matches("^[а-яА-яa-zA-Z]{2,30}$")) {
            return "first name must be from 2 to 30 characters";
        }
        if (!request.getLastName().matches("^[а-яА-яa-zA-Z]{2,30}$")) {
            return "last name must be from 2 to 30 characters";
        }
        if (!request.getMiddleName().isEmpty() &&
                !request.getMiddleName().matches("^[а-яА-яa-zA-Z]{2,30}$")) {
            return "middle name must be from 2 to 30 characters";
        }
        BigDecimal summa = request.getAmount();
        int term = request.getTerm();

        if (summa.compareTo(new BigDecimal("20000")) <= 0 && term < 6)
            return "term must be more 6 months and amount of money must be more 20000";

        LocalDate person = request.getBirthdate();
        LocalDate now = LocalDate.now();

        if(now.minusYears(18).isBefore(person))
            return "your age must be more 18 years old";

        String email = request.getEmail();
        String emailRegex = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";

        if (!email.matches(emailRegex)) {
            return "invalid email";
        }

        String series = request.getPassportSeries();
        String number = request.getPassportNumber();

        if (!series.matches("^\\d{4}$") || !number.matches("^\\d{6}$")) {
            return "invalid passport";
        }

        return null;

    }



}

//Имя, Фамилия - от 2 до 30 латинских букв. Отчество, при наличии - от 2 до 30 латинских букв.
//Сумма кредита - действительно число, большее или равное 20000.
//Срок кредита - целое число, большее или равное 6.
//Дата рождения - число в формате гггг-мм-дд, не позднее 18 лет с текущего дня.
//Email адрес - строка, подходящая под паттерн ^[a-z0-9A-Z_!#$%&'*+/=?`{|}~^.-]+@[a-z0-9A-Z.-]+$
//Серия паспорта - 4 цифры, номер паспорта - 6 цифр.