package ru.neoflex.calculator.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.neoflex.calculator.config.CalculatorProperties;
import ru.neoflex.calculator.dto.CreditDto;
import ru.neoflex.calculator.dto.EmploymentDto;
import ru.neoflex.calculator.dto.PaymentScheduleElementDto;
import ru.neoflex.calculator.dto.ScoringDataDto;
import ru.neoflex.calculator.enums.EmploymentStatus;
import ru.neoflex.calculator.enums.Gender;
import ru.neoflex.calculator.enums.MaritalStatus;
import ru.neoflex.calculator.enums.Position;
import ru.neoflex.calculator.service.impl.CalculatorServiceImpl;
import ru.neoflex.calculator.util.CalculatorUtils;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class CalculatorServiceCalcImplTest {

    @Mock
    private CalculatorProperties properties;

    @Mock
    private CalculatorProperties.Insurance insurance;

    @Mock
    private CalculatorProperties.Threshold thresholds;

    @Mock
    private CalculatorProperties.Utils utils;

    @Mock
    private CalculatorProperties.Scoring scoring;

    @Mock
    private CalculatorProperties.Age age;

    @Mock
    private CalculatorProperties.Experience experience;

    @Mock
    private CalculatorProperties.RateAdjustments rateAdjustments;

    @Mock
    private CalculatorProperties.GenderAdjustment female;

    @Mock
    private CalculatorProperties.GenderAdjustment male;

    @InjectMocks
    private CalculatorServiceImpl service;

    private CalculatorUtils calculatorUtils;

    @BeforeEach
    void setUp() {
        lenient().when(properties.getBaseRate()).thenReturn(32);
        lenient().when(properties.getInsuranceDiscount()).thenReturn(8);
        lenient().when(properties.getInsurance()).thenReturn(insurance);
        lenient().when(insurance.getThresholds()).thenReturn(thresholds);
        lenient().when(insurance.getUtils()).thenReturn(utils);
        lenient().when(utils.getMonthlyDivisor()).thenReturn(1200);
        lenient().when(utils.getPercentConversion()).thenReturn(100);
        lenient().when(utils.getDaysInYear()).thenReturn(365);

        lenient().when(thresholds.getMaxS()).thenReturn(new BigDecimal("100000"));
        lenient().when(thresholds.getMaxM()).thenReturn(new BigDecimal("300000"));
        lenient().when(thresholds.getMaxL()).thenReturn(new BigDecimal("500000"));
        lenient().when(thresholds.getFeeS()).thenReturn(new BigDecimal("1000"));
        lenient().when(thresholds.getFeeM()).thenReturn(new BigDecimal("2000"));
        lenient().when(thresholds.getFeeL()).thenReturn(new BigDecimal("3000"));
        lenient().when(thresholds.getFeeXl()).thenReturn(new BigDecimal("5000"));

        lenient().when(properties.getScoring()).thenReturn(scoring);
        lenient().when(scoring.getAge()).thenReturn(age);
        lenient().when(age.getMin()).thenReturn(20);
        lenient().when(age.getMax()).thenReturn(65);
        lenient().when(scoring.getExperience()).thenReturn(experience);
        lenient().when(experience.getTotalMin()).thenReturn(18);
        lenient().when(experience.getCurrentMin()).thenReturn(3);

        lenient().when(scoring.getSalaryMultiplier()).thenReturn(new BigDecimal("24"));
        lenient().when(scoring.getRateAdjustments()).thenReturn(rateAdjustments);

        lenient().when(rateAdjustments.getFemale()).thenReturn(female);
        lenient().when(female.getAgeFrom()).thenReturn(32);
        lenient().when(female.getAgeTo()).thenReturn(60);
        lenient().when(female.getAdjustment()).thenReturn(new BigDecimal("-3"));
        lenient().when(rateAdjustments.getMale()).thenReturn(male);
        lenient().when(male.getAgeFrom()).thenReturn(30);
        lenient().when(male.getAgeTo()).thenReturn(55);
        lenient().when(male.getAdjustment()).thenReturn(new BigDecimal("-3"));

        calculatorUtils = new CalculatorUtils(properties);
        service = new CalculatorServiceImpl(properties, calculatorUtils);

    }

    @Test
    void calculateCredit_validData_shouldReturnCredit() {

        ScoringDataDto data = valid();

        CreditDto credit = service.calculateCredit(data);

        assertNotNull(credit);
        assertEquals(data.getTerm(), credit.getTerm());
        assertNotNull(credit.getMonthlyPayment());
        assertNotNull(credit.getRate());
    }

    @Test
    void calculateCredit_shouldGenerateCorrectScheduleSize() {

        ScoringDataDto data = valid();

        CreditDto credit = service.calculateCredit(data);

        assertEquals(data.getTerm(), credit.getPaymentSchedule().size());
    }

    @Test
    void calculateCredit_insuranceEnabled_shouldIncreaseAmount() {

        ScoringDataDto data = valid();
        data.setIsInsuranceEnabled(true);

        CreditDto credit = service.calculateCredit(data);

        assertTrue(credit.getAmount().compareTo(data.getAmount()) > 0);
    }
    @Test
    void calculateCredit_totalExperienceTooLow_shouldThrowException() {

        ScoringDataDto data = valid();

        data.getEmployment().setWorkExperienceTotal(6);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.calculateCredit(data)
        );
    }

    @Test
    void calculateCredit_currentExperienceTooLow_shouldThrowException() {

        ScoringDataDto data = valid();

        data.getEmployment().setWorkExperienceCurrent(1);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.calculateCredit(data)
        );
    }

    @Test
    void calculateCredit_insuranceDisabled_amountShouldEqualBase() {

        ScoringDataDto data = valid();
        data.setIsInsuranceEnabled(false);

        CreditDto credit = service.calculateCredit(data);

        assertEquals(data.getAmount(), credit.getAmount());
    }

    @Test
    void calculateCredit_unemployed_shouldThrowException() {

        ScoringDataDto data = valid();
        data.getEmployment().setEmploymentStatus(EmploymentStatus.UNEMPLOYED);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.calculateCredit(data)
        );
    }

    @Test
    void calculateCredit_ageTooLow_shouldThrowException() {
        ScoringDataDto data = valid();
        data.setBirthdate(LocalDate.now().minusYears(19));

        assertThrows(IllegalArgumentException.class,
                () -> service.calculateCredit(data));
    }

    @Test
    void calculateCredit_ageTooHigh_shouldThrowException() {
        ScoringDataDto data = valid();
        data.setBirthdate(LocalDate.now().minusYears(70));

        assertThrows(IllegalArgumentException.class,
                () -> service.calculateCredit(data));
    }
    @Test
    void calculateCredit_monthlyPayment_shouldBeCalculatedCorrectly() {

        ScoringDataDto data = valid();
        data.setIsInsuranceEnabled(false);

        CreditDto credit = service.calculateCredit(data);

        BigDecimal expectedPayment = new BigDecimal("9847.35");

        assertEquals(0,
                credit.getMonthlyPayment().compareTo(expectedPayment));
    }
    @Test
    void psk_shouldBeCalculated() {

        ScoringDataDto data = valid();

        CreditDto credit = service.calculateCredit(data);

        BigDecimal psk = credit.getPsk();

        assertNotNull(psk);
        assertTrue(psk.compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void calculateCredit_amountTooHigh_shouldThrowException() {
        ScoringDataDto data = valid();
        data.setAmount(new BigDecimal("10000000"));

        assertThrows(IllegalArgumentException.class,
                () -> service.calculateCredit(data));
    }

    @Test
    void calculateCredit_lastPayment_shouldCloseDebt() {
        ScoringDataDto data = valid();

        CreditDto credit = service.calculateCredit(data);

        PaymentScheduleElementDto last =
                credit.getPaymentSchedule().get(data.getTerm() - 1);

        assertEquals(BigDecimal.ZERO.setScale(2), last.getRemainingDebt().setScale(2));
    }

    public ScoringDataDto valid() {

        EmploymentDto employment = new EmploymentDto();
        employment.setEmploymentStatus(EmploymentStatus.EMPLOYED);
        employment.setPosition(Position.WORKER);
        employment.setSalary(new BigDecimal("100000"));
        employment.setWorkExperienceTotal(24);
        employment.setWorkExperienceCurrent(12);

        ScoringDataDto dto = new ScoringDataDto();

        dto.setAmount(new BigDecimal("100000"));
        dto.setTerm(12);
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setMiddleName("Test");
        dto.setGender(Gender.MALE);
        dto.setBirthdate(LocalDate.now().minusYears(22));

        dto.setPassportSeries("1234");
        dto.setPassportNumber("123456");
        dto.setPassportIssueDate(LocalDate.now().minusYears(5));
        dto.setPassportIssueBranch("Test branch");

        dto.setMaritalStatus(MaritalStatus.SINGLE);
        dto.setDependentAmount(0);
        dto.setEmployment(employment);

        dto.setAccountNumber("1234567890");
        dto.setIsInsuranceEnabled(true);
        dto.setIsSalaryClient(false);

        return dto;
    }

}

