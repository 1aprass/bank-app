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
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class StatementControllerSelectOfferTest {

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
    void selectOfferTestSuccess() throws Exception{
        LoanOfferDto request = createLoanOfferDto(true, true);

        doNothing().when(statementService).selectOffer(any(LoanOfferDto.class));

        mockMvc.perform(post("/statement/offer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(statementService).selectOffer(any());
    }

    @Test
    void selectOffer_invalidJson_shouldReturn400() throws Exception {
        String invalidJson = "{ Invalid Json } ";

        mockMvc.perform(post("/statement/offer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidJson)))
                .andExpect(status().isBadRequest());

        verify(statementService, never()).selectOffer(any());
    }

    @Test
    void selectOffer_serviceThrowsIllegalArgument_shouldReturn400() throws Exception {
        LoanOfferDto request = createLoanOfferDto(true, true);

        doThrow(new IllegalArgumentException()).when(statementService).selectOffer(any());
        mockMvc.perform(post("/statement/offer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(statementService).selectOffer(any());
    }

    @Test
    void selectOffers_ServiceThrowsIllegalArgument_ShouldReturnBadRequest() throws Exception {
        LoanOfferDto request = createLoanOfferDto(true, true);

        doThrow(new RuntimeException()).when(statementService).selectOffer(any());
        mockMvc.perform(post("/statement/offer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());

        verify(statementService).selectOffer(any());
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
