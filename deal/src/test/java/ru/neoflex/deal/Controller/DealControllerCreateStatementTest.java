package ru.neoflex.deal.Controller;

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
import ru.neoflex.deal.controller.DealController;
import ru.neoflex.deal.dto.LoanOfferDto;
import ru.neoflex.deal.dto.LoanStatementRequestDto;
import ru.neoflex.deal.exception.GlobalExceptionHandler;
import ru.neoflex.deal.service.DealService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class DealControllerCreateStatementTest {
    private MockMvc mockMvc;
    @Mock
    private DealService dealService;

    @InjectMocks
    private DealController dealController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders
                .standaloneSetup(dealController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

    }

    @Test
    void createStatementSuccess() throws Exception{
        LoanStatementRequestDto request = createValidLoanStatementRequest();
        List<LoanOfferDto> response = Arrays.asList(
                createLoanOfferDto(true, true),
                createLoanOfferDto(true, false),
                createLoanOfferDto(false, true),
                createLoanOfferDto(false, false)
        );

        when(dealService.createStatement(any(LoanStatementRequestDto.class))).thenReturn(response);

        mockMvc.perform(post("/deal/statement")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(4))
                .andExpect(jsonPath("$[*].statementId").exists())
                .andExpect(jsonPath("$[*].requestedAmount").isNotEmpty())
                .andExpect(jsonPath("$[*].totalAmount").isNotEmpty())
                .andExpect(jsonPath("$[*].term").isNotEmpty())
                .andExpect(jsonPath("$[*].monthlyPayment").isNotEmpty())
                .andExpect(jsonPath("$[*].rate").isNotEmpty())
                .andExpect(jsonPath("$[*].isInsuranceEnabled").exists())
                .andExpect(jsonPath("$[*].isSalaryClient").exists());

        verify(dealService, times(1)).createStatement(any(LoanStatementRequestDto.class));

    }

    @Test
    void createStatement_InvalidJson_ShouldReturnBadRequest() throws Exception {

        String invalidJson = "{ invalid json }";

        mockMvc.perform(post("/deal/statement")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        verify(dealService, never()).createStatement(any());
    }
    @Test
    void calculateOffers_ServiceThrowsIllegalArgument_ShouldReturnBadRequest() throws Exception {

        LoanStatementRequestDto request = createValidLoanStatementRequest();

        when(dealService.createStatement(any()))
                .thenThrow(new IllegalArgumentException("Invalid request"));

        mockMvc.perform(post("/deal/statement")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(dealService, times(1)).createStatement(any());
    }

    @Test
    void createStatement_ServiceThrowsException_ShouldReturnInternalServerError() throws Exception {

        LoanStatementRequestDto request = createValidLoanStatementRequest();

        when(dealService.createStatement(any()))
                .thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(post("/deal/statement")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());

        verify(dealService, times(1)).createStatement(any());
    }

    public LoanStatementRequestDto createValidLoanStatementRequest() {
        LoanStatementRequestDto request = new LoanStatementRequestDto();
        request.setAmount(new BigDecimal("500000"));
        request.setTerm(12);
        request.setFirstName("Ivan");
        request.setLastName("Ivanov");
        request.setMiddleName("Ivanovich");
        request.setEmail("ivan@example.com");
        request.setBirthdate(LocalDate.of(1990, 1, 1));
        request.setPassportSeries("1234");
        request.setPassportNumber("567890");
        return request;
    }

    public LoanOfferDto createLoanOfferDto(boolean insurance, boolean salary) {
        LoanOfferDto offer = new LoanOfferDto();

        offer.setStatementId(null);
        BigDecimal requestedAmount = new BigDecimal("500000");
        int term = 12;
        offer.setRequestedAmount(requestedAmount);
        offer.setTerm(term);

        BigDecimal rate = new BigDecimal("32");

        if (!salary) {
            rate = rate.add(new BigDecimal("28"));
        }
        if (insurance) {
            rate = rate.subtract(new BigDecimal("8"));
        }
        offer.setRate(rate);

        BigDecimal totalAmount;
        if (insurance) {
            totalAmount = requestedAmount.add(new BigDecimal("20000"));
        } else {
            totalAmount = requestedAmount;
        }
        offer.setTotalAmount(totalAmount);

        BigDecimal monthlyPayment;
        if (!insurance && !salary) {
            monthlyPayment = new BigDecimal("45000");
        } else if (insurance && !salary) {
            monthlyPayment = new BigDecimal("42000");
        } else if (!insurance && salary) {
            monthlyPayment = new BigDecimal("38000");
        } else {
            monthlyPayment = new BigDecimal("35000");
        }
        offer.setMonthlyPayment(monthlyPayment);

        offer.setIsInsuranceEnabled(insurance);
        offer.setIsSalaryClient(salary);

        return offer;
    }
}
