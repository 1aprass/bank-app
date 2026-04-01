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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class DealControllerSelectOfferTest {

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
    void selectOfferSuccess() throws Exception {
        LoanOfferDto request = createLoanOfferDto(false, false);
        doNothing().when(dealService).selectOffer(any());

        mockMvc.perform(post("/deal/offer/select")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
        verify(dealService, times(1)).selectOffer((any()));
    }

    @Test
    void selectOffer_invalidJson_shouldReturn400() throws Exception {

        String invalidJson = "{ invalid json }";
        mockMvc.perform(post("/deal/offer/select")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        verify(dealService, never()).selectOffer(any());
    }


    @Test
    void selectOffer_serviceThrowsIllegalArgument_shouldReturn400() throws Exception {

        LoanOfferDto request = createLoanOfferDto(true, true);

        doThrow(new IllegalArgumentException("Bad request"))
                .when(dealService).selectOffer(any());

        mockMvc.perform(post("/deal/offer/select")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(dealService).selectOffer(any());
    }

    @Test
    void calculateOffers_ServiceThrowsIllegalArgument_ShouldReturnBadRequest() throws Exception {

        LoanOfferDto request = createLoanOfferDto(true, true);

        doThrow(new IllegalArgumentException("Bad request"))
                .when(dealService).selectOffer(any());

        mockMvc.perform(post("/deal/offer/select")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(dealService).selectOffer(any());
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
