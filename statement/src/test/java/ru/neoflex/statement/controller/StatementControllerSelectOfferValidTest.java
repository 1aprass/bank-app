package ru.neoflex.statement.controller;

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
import ru.neoflex.statement.dto.LoanOfferDto;
import ru.neoflex.statement.exception.GlobalExceptionHandler;
import ru.neoflex.statement.service.StatementService;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class StatementControllerSelectOfferValidTest {
    private MockMvc mockMvc;

    @Mock
    private StatementService statementService;

    @InjectMocks
    private StatementController statementController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders
                .standaloneSetup(statementController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    @Test
    void shouldFail_whenStatementIdIsNull() throws Exception {
        LoanOfferDto dto = createLoanOfferDto(false, false);
        dto.setStatementId(null);

        mockMvc.perform(post("/statement/offer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
        verify(statementService, never()).selectOffer(any());
    }

    @Test
    void selectOffer_shouldFail_whenRequestedAmountIsNull() throws Exception {
        LoanOfferDto dto = createLoanOfferDto(false, false);
        dto.setRequestedAmount(null);

        mockMvc.perform(post("/statement/offer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verify(statementService, never()).selectOffer(any());
    }

    @Test
    void selectOffer_shouldFail_whenRequestedAmountIsZero() throws Exception {
        LoanOfferDto dto = createLoanOfferDto(false, false);
        dto.setRequestedAmount(BigDecimal.ZERO);

        mockMvc.perform(post("/statement/offer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verify(statementService, never()).selectOffer(any());
    }

    @Test
    void selectOffer_shouldFail_whenRequestedAmountNegative() throws Exception {
        LoanOfferDto dto = createLoanOfferDto(false, false);
        dto.setRequestedAmount(BigDecimal.valueOf(-1));

        mockMvc.perform(post("/statement/offer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verify(statementService, never()).selectOffer(any());
    }

    @Test
    void selectOffer_shouldFail_whenTotalAmountIsNull() throws Exception {
        LoanOfferDto dto = createLoanOfferDto(false, false);
        dto.setTotalAmount(null);

        mockMvc.perform(post("/statement/offer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verify(statementService, never()).selectOffer(any());
    }

    @Test
    void selectOffer_shouldFail_whenTotalAmountZero() throws Exception {
        LoanOfferDto dto = createLoanOfferDto(false, false);
        dto.setTotalAmount(BigDecimal.ZERO);

        mockMvc.perform(post("/statement/offer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verify(statementService, never()).selectOffer(any());
    }

    @Test
    void selectOffer_shouldFail_whenTotalAmountNegative() throws Exception {
        LoanOfferDto dto = createLoanOfferDto(false, false);
        dto.setTotalAmount(BigDecimal.valueOf(-1));
        mockMvc.perform(post("/statement/offer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verify(statementService, never()).selectOffer(any());
    }

    @Test
    void selectOffer_shouldFail_whenTermIsNull() throws Exception {
        LoanOfferDto dto = createLoanOfferDto(false, false);
        dto.setTerm(null);

        mockMvc.perform(post("/statement/offer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verify(statementService, never()).selectOffer(any());
    }

    @Test
    void selectOffer_shouldFail_whenMonthlyPaymentIsNull() throws Exception {
        LoanOfferDto dto = createLoanOfferDto(false, false);
        dto.setMonthlyPayment(null);

        mockMvc.perform(post("/statement/offer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verify(statementService, never()).selectOffer(any());
    }

    @Test
    void selectOffer_shouldFail_whenMonthlyPaymentZero() throws Exception {
        LoanOfferDto dto = createLoanOfferDto(false, false);
        dto.setMonthlyPayment(BigDecimal.ZERO);

        mockMvc.perform(post("/statement/offer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verify(statementService, never()).selectOffer(any());
    }

    @Test
    void selectOffer_shouldFail_whenMonthlyPaymentNegative() throws Exception {
        LoanOfferDto dto = createLoanOfferDto(false, false);
        dto.setMonthlyPayment(BigDecimal.valueOf(-100));

        mockMvc.perform(post("/statement/offer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verify(statementService, never()).selectOffer(any());
    }

    @Test
    void selectOffer_shouldFail_whenRateIsNull() throws Exception {
        LoanOfferDto dto = createLoanOfferDto(false, false);
        dto.setRate(null);

        mockMvc.perform(post("/statement/offer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verify(statementService, never()).selectOffer(any());
    }

    @Test
    void selectOffer_shouldFail_whenRateZero() throws Exception {
        LoanOfferDto dto = createLoanOfferDto(false, false);
        dto.setRate(BigDecimal.ZERO);

        mockMvc.perform(post("/statement/offer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verify(statementService, never()).selectOffer(any());
    }

    @Test
    void selectOffer_shouldFail_whenRateNegative() throws Exception {
        LoanOfferDto dto = createLoanOfferDto(false, false);
        dto.setRate(BigDecimal.valueOf(-1));

        mockMvc.perform(post("/statement/offer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verify(statementService, never()).selectOffer(any());
    }

    @Test
    void selectOffer_shouldFail_whenInsuranceEnabledIsNull() throws Exception {
        LoanOfferDto dto = createLoanOfferDto(false, false);
        dto.setIsInsuranceEnabled(null);

        mockMvc.perform(post("/statement/offer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verify(statementService, never()).selectOffer(any());
    }

    @Test
    void selectOffer_shouldFail_whenSalaryClientIsNull() throws Exception {
        LoanOfferDto dto = createLoanOfferDto(false, false);
        dto.setIsSalaryClient(null);

        mockMvc.perform(post("/statement/offer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verify(statementService, never()).selectOffer(any());
    }


    private LoanOfferDto createLoanOfferDto(boolean insurance, boolean salary) {
        LoanOfferDto dto = new LoanOfferDto();
        dto.setRequestedAmount(BigDecimal.valueOf(100000));
        dto.setTotalAmount(BigDecimal.valueOf(120000));
        dto.setTerm(12);
        dto.setMonthlyPayment(BigDecimal.valueOf(10000));
        dto.setRate(BigDecimal.valueOf(10));
        dto.setIsInsuranceEnabled(insurance);
        dto.setIsSalaryClient(salary);
        dto.setStatementId(UUID.randomUUID());
        return dto;
    }
}

