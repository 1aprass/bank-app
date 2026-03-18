package ru.neoflex.calculator.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import ru.neoflex.calculator.dto.EmploymentDto;
import ru.neoflex.calculator.dto.ScoringDataDto;
import ru.neoflex.calculator.enums.EmploymentStatus;
import ru.neoflex.calculator.enums.Gender;
import ru.neoflex.calculator.enums.MaritalStatus;
import ru.neoflex.calculator.enums.Position;
import ru.neoflex.calculator.exception.GlobalExceptionHandler;
import ru.neoflex.calculator.service.CalculatorService;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class CalculatorControllerCreditValidationTest {
    private MockMvc mockMvc;

    @Mock
    private CalculatorService calculatorService;

    @InjectMocks
    private CalculatorController calculatorController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders
                .standaloneSetup(calculatorController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    @Test
    void calculateCredit_InvalidTotalExperience_ShouldReturnBadRequest() throws Exception {
        ScoringDataDto request = createValidScoringData();
        request.getEmployment().setWorkExperienceTotal(-2);
        assertBadRequest(request);
    }

    @Test
    void calculateCredit_EmploymentWithoutCurrentExperience_ShouldReturnBadRequest() throws Exception {
        ScoringDataDto request = createValidScoringData();
        request.getEmployment().setWorkExperienceCurrent(null);
        assertBadRequest(request);
    }

    @Test
    void calculateCredit_EmploymentWithoutTotalExperience_ShouldReturnBadRequest() throws Exception {
        ScoringDataDto request = createValidScoringData();
        request.getEmployment().setWorkExperienceTotal(null);
        assertBadRequest(request);
    }

    @Test
    void calculateCredit_EmploymentWithoutINN_ShouldReturnBadRequest() throws Exception {
        ScoringDataDto request = createValidScoringData();
        request.getEmployment().setEmployerINN("");
        assertBadRequest(request);
    }
    @Test
    void calculateCredit_EmploymentValidInn_ShouldReturnBadRequest() throws Exception {
        ScoringDataDto request = createValidScoringData();
        request.getEmployment().setEmployerINN("123");
        assertBadRequest(request);
    }

    @Test
    void calculateCredit_EmploymentWithoutStatus_ShouldReturnBadRequest() throws Exception {
        ScoringDataDto request = createValidScoringData();
        request.getEmployment().setEmploymentStatus(null);
        assertBadRequest(request);
    }


    @Test
    void calculateCredit_EmploymentWithoutSalary_ShouldReturnBadRequest() throws Exception {
        ScoringDataDto request = createValidScoringData();
        request.getEmployment().setSalary(null);
        assertBadRequest(request);
    }
    @Test
    void calculateCredit_EmploymentNegativeSalary_ShouldReturnBadRequest() throws Exception {
        ScoringDataDto request = createValidScoringData();
        request.getEmployment().setSalary(new BigDecimal(-123));
        assertBadRequest(request);
    }

    @Test
    void calculateCredit_InvalidAmount_ShouldReturnBadRequest() throws Exception {
        ScoringDataDto request = createValidScoringData();
        request.setAmount(new BigDecimal(-123));
        assertBadRequest(request);
    }
    @Test
    void calculateCredit_WithoutAmount_ShouldReturnBadRequest() throws Exception {
        ScoringDataDto request = createValidScoringData();
        request.setAmount(null);
        assertBadRequest(request);
    }

    @Test
    void calculateCredit_WithoutTerm_ShouldReturnBadRequest() throws Exception {
        ScoringDataDto request = createValidScoringData();
        request.setTerm(null);
        assertBadRequest(request);
    }

    @Test
    void calculateCredit_InvalidTerm_ShouldReturnBadRequest() throws Exception {
        ScoringDataDto request = createValidScoringData();
        request.setTerm(5);
        assertBadRequest(request);
    }

    @Test
    void calculateCredit_WithoutGender_ShouldReturnBadRequest() throws Exception {

        ScoringDataDto request = createValidScoringData();
        request.setGender(null);
        assertBadRequest(request);
    }

    @Test
    void calculateCredit_BirthdateNull_ShouldReturnBadRequest() throws Exception {

        ScoringDataDto request = createValidScoringData();
        request.setBirthdate(null);
        assertBadRequest(request);
    }

    @Test
    void calculateCredit_BirthdateFuture_ShouldReturnBadRequest() throws Exception {

        ScoringDataDto request = createValidScoringData();
        request.setBirthdate(LocalDate.of(2027, 5, 2));
        assertBadRequest(request);
    }

    @Test
    void calculateCredit_BlankFirstName_ShouldReturnBadRequest() throws Exception {

        ScoringDataDto request = createValidScoringData();
        request.setFirstName("");
        assertBadRequest(request);
    }

    @Test
    void calculateCredit_BlankLastName_ShouldReturnBadRequest() throws Exception {

        ScoringDataDto request = createValidScoringData();
        request.setLastName("");
        assertBadRequest(request);
    }

    @Test
    void calculateCredit_WithInvalidFirstName_ShouldReturnBadRequest() throws Exception {
        ScoringDataDto request = createValidScoringData();
        request.setFirstName("Иван");
        assertBadRequest(request);
    }

    @Test
    void calculateCredit_WithInvalidSecondName_ShouldReturnBadRequest() throws Exception {
        ScoringDataDto request = createValidScoringData();
        request.setLastName("Иванов");
        assertBadRequest(request);
    }

    @Test
    void calculateCredit_WithInvalidMiddleName_ShouldReturnBadRequest() throws Exception {
        ScoringDataDto request = createValidScoringData();
        request.setMiddleName("Иванович");
        assertBadRequest(request);
    }
    @Test
    void calculateCredit_PassportIssueDateNull_ShouldReturnBadRequest() throws Exception {
        ScoringDataDto request = createValidScoringData();
        request.setPassportIssueDate(null);
        assertBadRequest(request);
    }
    @Test
    void calculateCredit_PassportIssueDateFuture_ShouldReturnBadRequest() throws Exception {
        ScoringDataDto request = createValidScoringData();
        request.setPassportIssueDate(LocalDate.now().plusDays(1));
        assertBadRequest(request);
    }
    @Test
    void calculateCredit_PassportIssueBranchNull_ShouldReturnBadRequest() throws Exception {
        ScoringDataDto request = createValidScoringData();
        request.setPassportIssueBranch(null);
        assertBadRequest(request);
    }
    @Test
    void calculateCredit_MaritalStatusNull_ShouldReturnBadRequest() throws Exception {
        ScoringDataDto request = createValidScoringData();
        request.setMaritalStatus(null);
        assertBadRequest(request);
    }

    @Test
    void calculateCredit_DependentAmountNull_ShouldReturnBadRequest() throws Exception {
        ScoringDataDto request = createValidScoringData();
        request.setDependentAmount(null);
        assertBadRequest(request);
    }

    @Test
    void calculateCredit_EmploymentNull_ShouldReturnBadRequest() throws Exception {
        ScoringDataDto request = createValidScoringData();
        request.setEmployment(null);
        assertBadRequest(request);
    }

    @Test
    void calculateCredit_AccountNumberNull_ShouldReturnBadRequest() throws Exception {
        ScoringDataDto request = createValidScoringData();
        request.setAccountNumber(null);
        assertBadRequest(request);
    }

    @Test
    void calculateCredit_IsInsuranceEnabledNull_ShouldReturnBadRequest() throws Exception {
        ScoringDataDto request = createValidScoringData();
        request.setIsInsuranceEnabled(null);
        assertBadRequest(request);
    }

    @Test
    void calculateCredit_IsSalaryClientNull_ShouldReturnBadRequest() throws Exception {
        ScoringDataDto request = createValidScoringData();
        request.setIsSalaryClient(null);
        assertBadRequest(request);
    }
    @Test
    void calculateCredit_InvalidPassportSeries_ShouldReturnBadRequest() throws Exception {

        ScoringDataDto request = createValidScoringData();
        request.setPassportSeries("12");
        assertBadRequest(request);
    }

    @Test
    void calculateCredit_NullPassportSeries_ShouldReturnBadRequest() throws Exception {

        ScoringDataDto request = createValidScoringData();
        request.setPassportSeries("");
        assertBadRequest(request);
    }

    @Test
    void calculateCredit_InvalidPassportNumber_ShouldReturnBadRequest() throws Exception {

        ScoringDataDto request = createValidScoringData();
        request.setPassportNumber("12345");
        assertBadRequest(request);
    }

    @Test
    void calculateCredit_NullPassportNumber_ShouldReturnBadRequest() throws Exception {

        ScoringDataDto request = createValidScoringData();
        request.setPassportNumber("");
        assertBadRequest(request);
    }

    private void assertBadRequest(ScoringDataDto request) throws Exception {
        mockMvc.perform(post("/calculator/calc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        verify(calculatorService, never()).calculateCredit(any());
    }

    public ScoringDataDto createValidScoringData() {
        ScoringDataDto dto = new ScoringDataDto();
        dto.setAmount(new BigDecimal("500000"));
        dto.setTerm(12);
        dto.setFirstName("Ivan");
        dto.setLastName("Ivanov");
        dto.setMiddleName("Ivanovich");
        dto.setGender(Gender.MALE);
        dto.setBirthdate(LocalDate.of(1990, 1, 1));
        dto.setPassportSeries("1234");
        dto.setPassportNumber("567890");
        dto.setPassportIssueDate(LocalDate.of(2010, 1, 1));
        dto.setPassportIssueBranch("MVD");
        dto.setMaritalStatus(MaritalStatus.SINGLE);
        dto.setDependentAmount(0);

        EmploymentDto employment = new EmploymentDto();
        employment.setEmploymentStatus(EmploymentStatus.EMPLOYED);
        employment.setEmployerINN("1234567890");
        employment.setSalary(new BigDecimal("50000"));
        employment.setPosition(Position.WORKER);
        employment.setWorkExperienceTotal(120);
        employment.setWorkExperienceCurrent(60);
        dto.setEmployment(employment);

        dto.setAccountNumber("40817810000000000001");
        dto.setIsInsuranceEnabled(true);
        dto.setIsSalaryClient(false);

        return dto;
    }

}
