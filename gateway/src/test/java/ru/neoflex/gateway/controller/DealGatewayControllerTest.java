package ru.neoflex.gateway.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import ru.neoflex.gateway.client.DealGatewayClient;
import ru.neoflex.gateway.client.StatementGatewayClient;
import ru.neoflex.gateway.dto.LoanOfferDto;
import ru.neoflex.gateway.dto.StatementDto;
import ru.neoflex.gateway.exception.GlobalExceptionHandler;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class DealGatewayControllerTest {

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
    void sendDocuments_shouldReturnOk() throws Exception {
        String statementId = UUID.randomUUID().toString();
        mockMvc.perform(post("/api/document/" + statementId + "/send"))
                .andExpect(status().isOk());
    }

    @Test
    void requestSignDocuments_shouldReturnOk() throws Exception {
        String statementId = UUID.randomUUID().toString();
        mockMvc.perform(post("/api/document/" + statementId + "/sign"))
                .andExpect(status().isOk());
    }

    @Test
    void signDocuments_shouldReturnOk() throws Exception {
        String statementId = UUID.randomUUID().toString();
        mockMvc.perform(post("/api/document/" + statementId + "/code")
                        .param("code", "1234"))
                .andExpect(status().isOk());
    }

    @Test
    void getStatementById_shouldReturnOk() throws Exception {
        String statementId = UUID.randomUUID().toString();
        when(dealClient.getStatementById(statementId)).thenReturn(new StatementDto());

        mockMvc.perform(get("/api/admin/statement/" + statementId))
                .andExpect(status().isOk());
    }

    @Test
    void getAllStatements_shouldReturnOk() throws Exception {
        when(dealClient.getAllStatements())
                .thenReturn(List.of(new StatementDto(), new StatementDto()));

        mockMvc.perform(get("/api/admin/statement"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2));
    }

    @Test
    void getAllStatements_returnsNull_shouldReturnEmptyList() throws Exception {
        when(dealClient.getAllStatements()).thenReturn(null);

        mockMvc.perform(get("/api/admin/statement"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(0));
    }

    @Test
    void getAllStatements_serviceThrowsException_shouldReturnInternalServerError() throws Exception {
        when(dealClient.getAllStatements())
                .thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(get("/api/admin/statement"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void sendDocuments_serviceThrowsException_shouldReturnInternalServerError() throws Exception {
        String statementId = UUID.randomUUID().toString();
        doThrow(new RuntimeException("Deal service unavailable"))
                .when(dealClient).sendDocuments(statementId);

        mockMvc.perform(post("/api/document/" + statementId + "/send"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void requestSignDocuments_serviceThrowsException_shouldReturnInternalServerError() throws Exception {
        String statementId = UUID.randomUUID().toString();
        doThrow(new RuntimeException("Deal service unavailable"))
                .when(dealClient).requestSignDocuments(statementId);

        mockMvc.perform(post("/api/document/" + statementId + "/sign"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void signDocuments_missingCodeParam_shouldReturnBadRequest() throws Exception {
        String statementId = UUID.randomUUID().toString();

        // не передаём обязательный @RequestParam code
        mockMvc.perform(post("/api/document/" + statementId + "/code"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getStatementById_serviceThrowsException_shouldReturnInternalServerError() throws Exception {
        String statementId = UUID.randomUUID().toString();
        when(dealClient.getStatementById(statementId))
                .thenThrow(new RuntimeException("Deal service unavailable"));

        mockMvc.perform(get("/api/admin/statement/" + statementId))
                .andExpect(status().isInternalServerError());
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