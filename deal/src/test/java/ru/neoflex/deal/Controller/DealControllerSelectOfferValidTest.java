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
import ru.neoflex.deal.exception.GlobalExceptionHandler;
import ru.neoflex.deal.service.DealService;

import java.math.BigDecimal;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class DealControllerSelectOfferValidTest {
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
    void shouldFail_whenStatementIdIsNull() throws Exception {
        LoanOfferDto dto = createLoanOfferDto(false, false);
        dto.setStatementId(null);

        mockMvc.perform(post("/deal/offer/select")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFail_whenRequestedAmountIsNull() throws Exception {
        LoanOfferDto dto = createLoanOfferDto(false, false);
        dto.setRequestedAmount(null);

        mockMvc.perform(post("/deal/offer/select")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFail_whenRequestedAmountIsZero() throws Exception {
        LoanOfferDto dto = createLoanOfferDto(false, false);
        dto.setRequestedAmount(BigDecimal.ZERO);

        mockMvc.perform(post("/deal/offer/select")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFail_whenRequestedAmountNegative() throws Exception {
        LoanOfferDto dto = createLoanOfferDto(false, false);
        dto.setRequestedAmount(BigDecimal.valueOf(-1));

        mockMvc.perform(post("/deal/offer/select")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFail_whenTotalAmountIsNull() throws Exception {
        LoanOfferDto dto = createLoanOfferDto(false, false);
        dto.setTotalAmount(null);

        mockMvc.perform(post("/deal/offer/select")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFail_whenTotalAmountZero() throws Exception {
        LoanOfferDto dto = createLoanOfferDto(false, false);
        dto.setTotalAmount(BigDecimal.ZERO);

        mockMvc.perform(post("/deal/offer/select")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFail_whenTotalAmountNegative() throws Exception {
        LoanOfferDto dto = createLoanOfferDto(false, false);
        dto.setTotalAmount(BigDecimal.valueOf(-1));
        mockMvc.perform(post("/deal/offer/select")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFail_whenTermIsNull() throws Exception {
        LoanOfferDto dto = createLoanOfferDto(false, false);
        dto.setTerm(null);

        mockMvc.perform(post("/deal/offer/select")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFail_whenMonthlyPaymentIsNull() throws Exception {
        LoanOfferDto dto = createLoanOfferDto(false, false);
        dto.setMonthlyPayment(null);

        mockMvc.perform(post("/deal/offer/select")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFail_whenMonthlyPaymentZero() throws Exception {
        LoanOfferDto dto = createLoanOfferDto(false, false);
        dto.setMonthlyPayment(BigDecimal.ZERO);

        mockMvc.perform(post("/deal/offer/select")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFail_whenMonthlyPaymentNegative() throws Exception {
        LoanOfferDto dto = createLoanOfferDto(false, false);
        dto.setMonthlyPayment(BigDecimal.valueOf(-100));

        mockMvc.perform(post("/deal/offer/select")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFail_whenRateIsNull() throws Exception {
        LoanOfferDto dto = createLoanOfferDto(false, false);
        dto.setRate(null);

        mockMvc.perform(post("/deal/offer/select")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFail_whenRateZero() throws Exception {
        LoanOfferDto dto = createLoanOfferDto(false, false);
        dto.setRate(BigDecimal.ZERO);

        mockMvc.perform(post("/deal/offer/select")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFail_whenRateNegative() throws Exception {
        LoanOfferDto dto = createLoanOfferDto(false, false);
        dto.setRate(BigDecimal.valueOf(-1));

        mockMvc.perform(post("/deal/offer/select")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFail_whenInsuranceEnabledIsNull() throws Exception {
        LoanOfferDto dto = createLoanOfferDto(false, false);
        dto.setIsInsuranceEnabled(null);

        mockMvc.perform(post("/deal/offer/select")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFail_whenSalaryClientIsNull() throws Exception {
        LoanOfferDto dto = createLoanOfferDto(false, false);
        dto.setIsSalaryClient(null);

        mockMvc.perform(post("/deal/offer/select")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
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
