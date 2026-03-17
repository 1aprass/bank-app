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
import ru.neoflex.calculator.dto.ScoringDataDto;
import ru.neoflex.calculator.enums.EmploymentStatus;
import ru.neoflex.calculator.enums.Gender;
import ru.neoflex.calculator.enums.MaritalStatus;
import ru.neoflex.calculator.enums.Position;
import ru.neoflex.calculator.service.impl.CalculatorServiceImpl;
import ru.neoflex.calculator.util.CalculatorUtils;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
public class CalculatorServiceCalcImplRateTest {

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

        lenient().when(rateAdjustments.getSelfEmployed()).thenReturn(new BigDecimal("2"));
        lenient().when(rateAdjustments.getBusinessOwner()).thenReturn(new BigDecimal("1"));
        lenient().when(rateAdjustments.getMidManager()).thenReturn(new BigDecimal("-2"));
        lenient().when(rateAdjustments.getTopManager()).thenReturn(new BigDecimal("-3"));
        lenient().when(rateAdjustments.getMarried()).thenReturn(new BigDecimal("-3"));
        lenient().when(rateAdjustments.getDivorced()).thenReturn(new BigDecimal("1"));

        lenient().when(rateAdjustments.getFemale()).thenReturn(female);
        lenient().when(female.getAgeFrom()).thenReturn(32);
        lenient().when(female.getAgeTo()).thenReturn(60);
        lenient().when(female.getAdjustment()).thenReturn(new BigDecimal("-3"));

        lenient().when(rateAdjustments.getMale()).thenReturn(male);
        lenient().when(male.getAgeFrom()).thenReturn(30);
        lenient().when(male.getAgeTo()).thenReturn(55);
        lenient().when(male.getAdjustment()).thenReturn(new BigDecimal("-3"));

        lenient().when(rateAdjustments.getNonBinary()).thenReturn(new BigDecimal("7"));

        calculatorUtils = new CalculatorUtils(properties);
        service = new CalculatorServiceImpl(properties, calculatorUtils);

    }

    @Test
    void calculateCredit_rateAdjustment_businessOwner_shouldIncreaseRate() {

        ScoringDataDto data = valid();
        data.getEmployment().setEmploymentStatus(EmploymentStatus.BUSINESS_OWNER);

        CreditDto credit = service.calculateCredit(data);

        BigDecimal expectedRate = BigDecimal.valueOf(32)
                .add(new BigDecimal("1"))
                .subtract(new BigDecimal("8"));

        assertEquals(0, credit.getRate().compareTo(expectedRate));
    }

    @Test
    void rateAdjustment_topManager_shouldDecreaseRate() {

        ScoringDataDto data = valid();
        data.getEmployment().setPosition(Position.TOP_MANAGER);

        CreditDto credit = service.calculateCredit(data);

        BigDecimal expectedRate = BigDecimal.valueOf(32)
                .add(new BigDecimal("-3"))
                .subtract(new BigDecimal("8"));

        assertEquals(0, credit.getRate().compareTo(expectedRate));
    }

    @Test
    void calculateCredit_rateAdjustment_divorced_shouldIncreaseRate() {

        ScoringDataDto data = valid();
        data.setMaritalStatus(MaritalStatus.DIVORCED);

        CreditDto credit = service.calculateCredit(data);

        BigDecimal expectedRate = BigDecimal.valueOf(32)
                .add(new BigDecimal("1"))
                .subtract(new BigDecimal("8"));

        assertEquals(0, credit.getRate().compareTo(expectedRate));
    }

    @Test
    void calculateCredit_rateAdjustment_nonBinary_shouldIncreaseRate() {

        ScoringDataDto data = valid();
        data.setGender(Gender.NON_BINARY);

        CreditDto credit = service.calculateCredit(data);

        BigDecimal expectedRate = BigDecimal.valueOf(32)
                .add(new BigDecimal("7"))
                .subtract(new BigDecimal("8"));

        assertEquals(0, credit.getRate().compareTo(expectedRate));
    }

    @Test
    void calculateCredit_rateAdjustment_maleAgeRange_shouldApply() {
        ScoringDataDto data = valid();
        data.setGender(Gender.MALE);
        data.setBirthdate(LocalDate.now().minusYears(35));
        CreditDto credit = service.calculateCredit(data);

        BigDecimal expectedRate = BigDecimal.valueOf(32 - 8 - 3);
        assertEquals(0, credit.getRate().compareTo(expectedRate));
    }



    @Test
    void calculateCredit_rateAdjustment_appliesForSelfEmployed() {
        ScoringDataDto data = valid();
        data.getEmployment().setEmploymentStatus(EmploymentStatus.SELF_EMPLOYED);

        CreditDto credit = service.calculateCredit(data);

        BigDecimal expectedRate = BigDecimal.valueOf(32 + 2 - (data.getIsInsuranceEnabled() ? 8 : 0));
        assertEquals(0, credit.getRate().compareTo(expectedRate));
    }

    @Test
    void calculateCredit_rateAdjustment_appliesForMidManagerPosition() {
        ScoringDataDto data = valid();
        data.getEmployment().setPosition(Position.MID_MANAGER);

        CreditDto credit = service.calculateCredit(data);

        BigDecimal expectedRate = BigDecimal.valueOf(32 - 2 - (data.getIsInsuranceEnabled() ? 8 : 0));
        assertEquals(0, credit.getRate().compareTo(expectedRate));
    }

    @Test
    void calculateCredit_rateAdjustment_appliesForMarried() {
        ScoringDataDto data = valid();
        data.setMaritalStatus(MaritalStatus.MARRIED);

        CreditDto credit = service.calculateCredit(data);

        BigDecimal expectedRate = BigDecimal.valueOf(32 - 3 - (data.getIsInsuranceEnabled() ? 8 : 0));
        assertEquals(0, credit.getRate().compareTo(expectedRate));
    }

    @Test
    void calculateCredit_rateAdjustment_appliesForFemaleAgeRange() {
        ScoringDataDto data = valid();
        data.setGender(Gender.FEMALE);
        data.setBirthdate(LocalDate.now().minusYears(35));

        CreditDto credit = service.calculateCredit(data);

        BigDecimal expectedRate = BigDecimal.valueOf(32 - 8 - 3);
        assertEquals(0, credit.getRate().compareTo(expectedRate));
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
