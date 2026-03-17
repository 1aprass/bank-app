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
import ru.neoflex.calculator.dto.LoanStatementRequestDto;
import ru.neoflex.calculator.exception.GlobalExceptionHandler;
import ru.neoflex.calculator.service.CalculatorService;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class CalculatorControllerOffersValidationTest {
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
    void calculateOffers_WithSmallAmount_ShouldReturnBadRequest() throws Exception {
        LoanStatementRequestDto invalidRequest = new LoanStatementRequestDto();
        invalidRequest.setAmount(new BigDecimal("1000"));

        mockMvc.perform(post("/calculator/offers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(calculatorService, never()).calculateOffers(any());
    }

    @Test
    void calculateOffers_WithSmallTerm_ShouldReturnBadRequest() throws Exception {
        LoanStatementRequestDto invalidRequest = new LoanStatementRequestDto();
        invalidRequest.setTerm(5);

        mockMvc.perform(post("/calculator/offers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(calculatorService, never()).calculateOffers(any());
    }

    @Test
    void calculateOffers_WithInvalidFirstName_ShouldReturnBadRequest() throws Exception {
        LoanStatementRequestDto invalidRequest = new LoanStatementRequestDto();
        invalidRequest.setFirstName("Иван");

        mockMvc.perform(post("/calculator/offers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(calculatorService, never()).calculateOffers(any());
    }

    @Test
    void calculateOffers_WithInvalidSecondName_ShouldReturnBadRequest() throws Exception {
        LoanStatementRequestDto invalidRequest = new LoanStatementRequestDto();
        invalidRequest.setLastName("Иванов");

        mockMvc.perform(post("/calculator/offers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(calculatorService, never()).calculateOffers(any());
    }

    @Test
    void calculateOffers_WithInvalidMiddleName_ShouldReturnBadRequest() throws Exception {
        LoanStatementRequestDto invalidRequest = new LoanStatementRequestDto();
        invalidRequest.setMiddleName("Иванович");

        mockMvc.perform(post("/calculator/offers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(calculatorService, never()).calculateOffers(any());
    }

    @Test
    void calculateOffers_AmountNull_ShouldReturnBadRequest() throws Exception {

        LoanStatementRequestDto request = createValidLoanStatementRequest();
        request.setAmount(null);

        mockMvc.perform(post("/calculator/offers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(calculatorService, never()).calculateOffers(any());
    }

    @Test
    void calculateOffers_TermNull_ShouldReturnBadRequest() throws Exception {

        LoanStatementRequestDto request = createValidLoanStatementRequest();
        request.setTerm(null);

        mockMvc.perform(post("/calculator/offers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(calculatorService, never()).calculateOffers(any());
    }

    @Test
    void calculateOffers_InvalidEmail_ShouldReturnBadRequest() throws Exception {

        LoanStatementRequestDto request = createValidLoanStatementRequest();
        request.setEmail("invalid-email");

        mockMvc.perform(post("/calculator/offers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(calculatorService, never()).calculateOffers(any());
    }

    @Test
    void calculateOffers_NullEmail_ShouldReturnBadRequest() throws Exception {

        LoanStatementRequestDto request = createValidLoanStatementRequest();
        request.setEmail("");

        mockMvc.perform(post("/calculator/offers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(calculatorService, never()).calculateOffers(any());
    }

    @Test
    void calculateOffers_InvalidPassportSeries_ShouldReturnBadRequest() throws Exception {

        LoanStatementRequestDto request = createValidLoanStatementRequest();
        request.setPassportSeries("12");

        mockMvc.perform(post("/calculator/offers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(calculatorService, never()).calculateOffers(any());
    }

    @Test
    void calculateOffers_NullPassportSeries_ShouldReturnBadRequest() throws Exception {

        LoanStatementRequestDto request = createValidLoanStatementRequest();
        request.setPassportSeries("");

        mockMvc.perform(post("/calculator/offers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(calculatorService, never()).calculateOffers(any());
    }

    @Test
    void calculateOffers_InvalidPassportNumber_ShouldReturnBadRequest() throws Exception {

        LoanStatementRequestDto request = createValidLoanStatementRequest();
        request.setPassportNumber("12345");

        mockMvc.perform(post("/calculator/offers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(calculatorService, never()).calculateOffers(any());
    }

    @Test
    void calculateOffers_NullPassportNumber_ShouldReturnBadRequest() throws Exception {

        LoanStatementRequestDto request = createValidLoanStatementRequest();
        request.setPassportNumber("");

        mockMvc.perform(post("/calculator/offers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(calculatorService, never()).calculateOffers(any());
    }

    @Test
    void calculateOffers_BlankFirstName_ShouldReturnBadRequest() throws Exception {

        LoanStatementRequestDto request = createValidLoanStatementRequest();
        request.setFirstName("");

        mockMvc.perform(post("/calculator/offers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(calculatorService, never()).calculateOffers(any());
    }

    @Test
    void calculateOffers_BirthdateNull_ShouldReturnBadRequest() throws Exception {

        LoanStatementRequestDto request = createValidLoanStatementRequest();
        request.setBirthdate(null);

        mockMvc.perform(post("/calculator/offers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(calculatorService, never()).calculateOffers(any());
    }

    @Test
    void calculateOffers_BirthdateFuture_ShouldReturnBadRequest() throws Exception {

        LoanStatementRequestDto request = createValidLoanStatementRequest();
        request.setBirthdate(LocalDate.of(2027, 5, 2));

        mockMvc.perform(post("/calculator/offers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(calculatorService, never()).calculateOffers(any());
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


}
