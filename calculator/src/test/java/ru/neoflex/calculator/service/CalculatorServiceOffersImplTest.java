package ru.neoflex.calculator.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.neoflex.calculator.config.CalculatorProperties;
import ru.neoflex.calculator.dto.LoanOfferDto;
import ru.neoflex.calculator.dto.LoanStatementRequestDto;
import ru.neoflex.calculator.service.impl.CalculatorServiceImpl;
import ru.neoflex.calculator.util.CalculatorUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class CalculatorServiceOffersImplTest {

    @Mock
    private CalculatorProperties properties;

    @Mock
    private CalculatorUtils calculatorUtils;

    @Mock
    private CalculatorProperties.Insurance insurance;

    @Mock
    private CalculatorProperties.Threshold thresholds;

    @InjectMocks
    private CalculatorServiceImpl service;

    private LoanStatementRequestDto request;

    @BeforeEach
    void setUp() {
        request = new LoanStatementRequestDto();
        request.setAmount(new BigDecimal("100000"));
        request.setTerm(12);
        request.setBirthdate(LocalDate.now().minusYears(25));
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john.doe@example.com");
        request.setPassportSeries("1234");
        request.setPassportNumber("123456");

        lenient().when(properties.getBaseRate()).thenReturn(32);
        lenient().when(properties.getSalaryClientBonus()).thenReturn(28);
        lenient().when(properties.getInsuranceDiscount()).thenReturn(8);

        lenient().when(properties.getInsurance()).thenReturn(insurance);
        lenient().when(insurance.getThresholds()).thenReturn(thresholds);

        lenient().when(thresholds.getMaxS()).thenReturn(new BigDecimal("100000"));
        lenient().when(thresholds.getMaxM()).thenReturn(new BigDecimal("300000"));
        lenient().when(thresholds.getMaxL()).thenReturn(new BigDecimal("500000"));
        lenient().when(thresholds.getFeeS()).thenReturn(new BigDecimal("1000"));
        lenient().when(thresholds.getFeeM()).thenReturn(new BigDecimal("2000"));
        lenient().when(thresholds.getFeeL()).thenReturn(new BigDecimal("3000"));
        lenient().when(thresholds.getFeeXl()).thenReturn(new BigDecimal("5000"));

        lenient().when(calculatorUtils.calculateMonthlyPayment(any(BigDecimal.class),
                        any(BigDecimal.class), anyInt()))
                .thenReturn(new BigDecimal("10000"));
    }

    @Test
    void calculateOffers_returnsFourOffers() {
        List<LoanOfferDto> offers = service.calculateOffers(request);
        assertEquals(4, offers.size());
    }

    @Test
    void calculateOffers_under18_throwsException() {
        request.setBirthdate(LocalDate.now().minusYears(16));
        assertThrows(IllegalArgumentException.class, () -> service.calculateOffers(request));
    }


    @Test
    void salaryClient_rateShouldNotIncrease() {
        List<LoanOfferDto> offers = service.calculateOffers(request);
        LoanOfferDto salaryOffer = offers.stream()
                .filter(LoanOfferDto::getIsSalaryClient)
                .findFirst()
                .orElseThrow();
        assertEquals(new BigDecimal("32"), salaryOffer.getRate());
    }

    @Test
    void insuranceEnabled_rateShouldDecrease() {
        List<LoanOfferDto> offers = service.calculateOffers(request);

        LoanOfferDto offer = offers.stream()
                .filter(o -> o.getIsInsuranceEnabled() && o.getIsSalaryClient())
                .findFirst()
                .orElseThrow();
        assertEquals(new BigDecimal("24"), offer.getRate());
    }

    @Test
    void insuranceDisabled_totalAmountEqualsBase() {
        List<LoanOfferDto> offers = service.calculateOffers(request);
        LoanOfferDto offer = offers.stream()
                .filter(o -> !o.getIsInsuranceEnabled())
                .findFirst()
                .orElseThrow();
        assertEquals(request.getAmount(), offer.getTotalAmount());
    }

    @Test
    void amountLessThanMaxS_addsFeeS() {
        request.setAmount(new BigDecimal("90000"));
        List<LoanOfferDto> offers = service.calculateOffers(request);
        LoanOfferDto offer = offers.stream()
                .filter(LoanOfferDto::getIsInsuranceEnabled)
                .findFirst()
                .orElseThrow();
        assertEquals(new BigDecimal("91000"), offer.getTotalAmount());
    }

    @Test
    void amountLessThanMaxM_addsFeeM() {
        request.setAmount(new BigDecimal("200000"));
        List<LoanOfferDto> offers = service.calculateOffers(request);
        LoanOfferDto offer = offers.stream()
                .filter(LoanOfferDto::getIsInsuranceEnabled)
                .findFirst()
                .orElseThrow();
        assertEquals(new BigDecimal("202000"), offer.getTotalAmount());
    }

    @Test
    void amountLessThanMaxL_addsFeeL() {
        request.setAmount(new BigDecimal("400000"));
        List<LoanOfferDto> offers = service.calculateOffers(request);
        LoanOfferDto offer = offers.stream()
                .filter(LoanOfferDto::getIsInsuranceEnabled)
                .findFirst()
                .orElseThrow();
        assertEquals(new BigDecimal("403000"), offer.getTotalAmount());
    }

    @Test
    void amountGreaterThanMaxL_addsFeeXL() {
        request.setAmount(new BigDecimal("700000"));
        List<LoanOfferDto> offers = service.calculateOffers(request);
        LoanOfferDto offer = offers.stream()
                .filter(LoanOfferDto::getIsInsuranceEnabled)
                .findFirst()
                .orElseThrow();
        assertEquals(new BigDecimal("705000"), offer.getTotalAmount());
    }


    @Test
    void offers_sortedByRateDescending() {
        List<LoanOfferDto> offers = service.calculateOffers(request);
        for (int i = 0; i < offers.size() - 1; i++) {
            assertTrue(offers.get(i).getRate().compareTo(offers.get(i + 1).getRate()) >= 0);
        }
    }

    @Test
    void offers_prescoring_BirthdateNull_ShouldThrowException() {
        LoanStatementRequestDto request = new LoanStatementRequestDto();
        request.setBirthdate(null);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.prescoring(request)
        );

        assertEquals("Birthdate is required", ex.getMessage());
    }
}
