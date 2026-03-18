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
import ru.neoflex.calculator.dto.CreditDto;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class CalculatorControllerCreditTest {

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
    void calculateCreditSuccess() throws Exception {

        ScoringDataDto request = createValidScoringData();
        CreditDto response = createCreditDto();

        when(calculatorService.calculateCredit(any())).thenReturn(response);

        mockMvc.perform(post("/calculator/calc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(500000))
                .andExpect(jsonPath("$.term").value(12));

        verify(calculatorService, times(1)).calculateCredit(any());
    }

    @Test
    void calculateCredit_ServiceIllegalArgument_ShouldReturnBadRequest() throws Exception {
        ScoringDataDto request = createValidScoringData();
        when(calculatorService.calculateCredit(any()))
                .thenThrow(new IllegalArgumentException("Invalid data"));

        mockMvc.perform(post("/calculator/calc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(calculatorService, times(1)).calculateCredit(any());
    }



    @Test
    void calculateCredit_ServiceException_ShouldReturnInternalServerError() throws Exception {
        ScoringDataDto request = createValidScoringData();
        when(calculatorService.calculateCredit(any()))
                .thenThrow(new RuntimeException("Server error"));

        mockMvc.perform(post("/calculator/calc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());

        verify(calculatorService, times(1)).calculateCredit(any());
    }

    @Test
    void calculateCredit_InvalidJson_ShouldReturnBadRequest() throws Exception {
        String invalidJson = "{ invalid json }";

        mockMvc.perform(post("/calculator/calc")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
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

    public CreditDto createCreditDto() {
        CreditDto credit = new CreditDto();
        credit.setAmount(new BigDecimal("500000"));
        credit.setTerm(12);
        credit.setMonthlyPayment(new BigDecimal("45000"));
        credit.setRate(new BigDecimal("12.5"));
        credit.setPsk(new BigDecimal("15"));
        credit.setIsInsuranceEnabled(true);
        credit.setIsSalaryClient(false);
        credit.setPaymentSchedule(null);
        return credit;
    }

}
