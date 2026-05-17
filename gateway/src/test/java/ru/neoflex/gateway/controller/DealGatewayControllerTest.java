package ru.neoflex.gateway.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestClient;
import ru.neoflex.gateway.dto.LoanOfferDto;
import ru.neoflex.gateway.dto.StatementDto;
import ru.neoflex.gateway.exception.GlobalExceptionHandler;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.lenient;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class DealGatewayControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock private RestClient dealRestClient;
    @Mock private RestClient statementRestClient;
    @Mock private RestClient.RequestBodyUriSpec bodyUriSpec;
    @Mock private RestClient.RequestBodySpec bodySpec;
    @Mock private RestClient.ResponseSpec responseSpec;
    @Mock private RestClient.RequestHeadersUriSpec headersUriSpec;
    @Mock private RestClient.RequestHeadersSpec headersSpec;

    @BeforeEach
    void setUp() {
        GatewayController controller = new GatewayController(dealRestClient, statementRestClient);
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        lenient().when(dealRestClient.post()).thenReturn(bodyUriSpec);
        lenient().when(bodyUriSpec.uri(anyString())).thenReturn(bodySpec);
        lenient().when(bodyUriSpec.uri(anyString(), any(Object.class))).thenReturn(bodySpec);
        lenient().when(bodyUriSpec.uri(anyString(), any(Object.class), any(Object.class))).thenReturn(bodySpec);
        lenient().when(bodyUriSpec.uri(anyString(), (Object[]) any())).thenReturn(bodySpec);
        lenient().when(bodySpec.body(any(Object.class))).thenReturn(bodySpec);
        lenient().when(bodySpec.retrieve()).thenReturn(responseSpec);
        lenient().when(responseSpec.toBodilessEntity()).thenReturn(ResponseEntity.ok().build());

        lenient().when(dealRestClient.get()).thenReturn(headersUriSpec);
        lenient().when(headersUriSpec.uri(anyString())).thenReturn(headersSpec);
        lenient().when(headersUriSpec.uri(anyString(), any(Object.class))).thenReturn(headersSpec);
        lenient().when(headersUriSpec.uri(anyString(), (Object[]) any())).thenReturn(headersSpec);
        lenient().when(headersSpec.retrieve()).thenReturn(responseSpec);
        lenient().when(bodySpec.retrieve()).thenReturn(responseSpec);
        lenient().when(responseSpec.toBodilessEntity()).thenReturn(ResponseEntity.ok().build());

    }

    @Test
    void selectOffer_shouldReturnOk() throws Exception {
        mockMvc.perform(post("/api/offer/select")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createOffer())))
                .andExpect(status().isOk());
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
        when(responseSpec.body(StatementDto.class)).thenReturn(new StatementDto());

        mockMvc.perform(get("/api/admin/statement/" + statementId))
                .andExpect(status().isOk());
    }

    @Test
    void getAllStatements_shouldReturnOk() throws Exception {
        when(responseSpec.body(any(ParameterizedTypeReference.class)))
                .thenReturn(List.of(new StatementDto(), new StatementDto()));

        mockMvc.perform(get("/api/admin/statement"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2));
    }

    @Test
    void getAllStatements_returnsNull_shouldReturnEmptyList() throws Exception {
        when(responseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(null);

        mockMvc.perform(get("/api/admin/statement"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(0));
    }

    @Test
    void getAllStatements_serviceThrowsException_shouldReturnInternalServerError() throws Exception {
        lenient().when(responseSpec.body(any(ParameterizedTypeReference.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(get("/api/admin/statement"))
                .andExpect(status().isInternalServerError());
    }
    @Test
    void selectOffer_invalidJson_shouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/offer/select")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ invalid json }"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void sendDocuments_serviceThrowsException_shouldReturnInternalServerError() throws Exception {
        String statementId = UUID.randomUUID().toString();
        lenient().when(responseSpec.toBodilessEntity())
                .thenThrow(new RuntimeException("Deal service unavailable"));

        mockMvc.perform(post("/api/document/" + statementId + "/send"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void requestSignDocuments_serviceThrowsException_shouldReturnInternalServerError() throws Exception {
        String statementId = UUID.randomUUID().toString();
        lenient().when(responseSpec.toBodilessEntity())
                .thenThrow(new RuntimeException("Deal service unavailable"));

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
        when(responseSpec.body(StatementDto.class))
                .thenThrow(new RuntimeException("Deal service unavailable"));

        mockMvc.perform(get("/api/admin/statement/" + statementId))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void selectOffer_serviceThrowsException_shouldReturnInternalServerError() throws Exception {
        lenient().when(responseSpec.toBodilessEntity())
                .thenThrow(new RuntimeException("Deal service unavailable"));

        mockMvc.perform(post("/api/offer/select")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createOffer())))
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