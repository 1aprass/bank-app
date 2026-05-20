package ru.neoflex.gateway.controller;

import ru.neoflex.gateway.client.DealGatewayClient;
import ru.neoflex.gateway.client.StatementGatewayClient;
import ru.neoflex.gateway.dto.LoanOfferDto;
import ru.neoflex.gateway.dto.LoanStatementRequestDto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.neoflex.gateway.exception.GlobalExceptionHandler;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.time.LocalDate;

@ExtendWith(MockitoExtension.class)
public class StatementGatewayControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private DealGatewayClient dealClient;

    @Mock
    private StatementGatewayClient statementClient;

    @BeforeEach
    void setUp() {
        GatewayController controller = new GatewayController(dealClient, statementClient);
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }


    @Test
    void prescoring_shouldReturnOffers() throws Exception {
        List<LoanOfferDto> offers = List.of(createOffer(), createOffer(), createOffer(), createOffer());
        when(statementClient.createStatement(any())).thenReturn(offers);

        mockMvc.perform(post("/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createValidRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(4));
    }

    @Test
    void prescoring_invalidJson_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ invalid json }"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void prescoring_serviceThrowsException_shouldReturnInternalServerError() throws Exception {
        when(statementClient.createStatement(any()))
                .thenThrow(new RuntimeException("Statement service unavailable"));

        mockMvc.perform(post("/api")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createValidRequest())))
                .andExpect(status().isInternalServerError());
    }


    @Test
    void selectOffer_shouldReturnOk() throws Exception {
        mockMvc.perform(post("/api/offer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createOffer())))
                .andExpect(status().isOk());
    }

    @Test
    void selectOffer_invalidJson_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/offer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ invalid json }"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void selectOffer_serviceThrowsException_shouldReturnInternalServerError() throws Exception {
        doThrow(new RuntimeException("Statement service unavailable"))
                .when(statementClient).selectOffer(any());

        mockMvc.perform(post("/api/offer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createOffer())))
                .andExpect(status().isInternalServerError());
    }


    private LoanStatementRequestDto createValidRequest() {
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

    private LoanOfferDto createOffer() {
        LoanOfferDto offer = new LoanOfferDto();
        offer.setStatementId(UUID.randomUUID());
        offer.setRequestedAmount(new BigDecimal("500000"));
        offer.setTotalAmount(new BigDecimal("520000"));
        offer.setTerm(12);
        offer.setMonthlyPayment(new BigDecimal("45000"));
        offer.setRate(new BigDecimal("15"));
        offer.setIsInsuranceEnabled(false);
        offer.setIsSalaryClient(false);
        return offer;
    }
}
