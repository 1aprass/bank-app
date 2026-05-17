package ru.neoflex.gateway.controller;

import ru.neoflex.gateway.dto.LoanOfferDto;
import ru.neoflex.gateway.dto.LoanStatementRequestDto;

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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class StatementGatewayControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock private RestClient dealRestClient;
    @Mock private RestClient statementRestClient;
    @Mock private RestClient.RequestBodyUriSpec bodyUriSpec;
    @Mock private RestClient.RequestBodySpec bodySpec;
    @Mock private RestClient.ResponseSpec responseSpec;

    @BeforeEach
    void setUp() {
        GatewayController controller = new GatewayController(dealRestClient, statementRestClient);
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        lenient().when(statementRestClient.post()).thenReturn(bodyUriSpec);
        lenient().when(bodyUriSpec.uri(anyString())).thenReturn(bodySpec);
        lenient().when(bodyUriSpec.uri(anyString(), any(Object.class))).thenReturn(bodySpec);
        lenient().when(bodyUriSpec.uri(anyString(), any(Object.class), any(Object.class))).thenReturn(bodySpec);
        lenient().when(bodyUriSpec.uri(anyString(), (Object[]) any())).thenReturn(bodySpec);
        lenient().when(bodySpec.body(any(Object.class))).thenReturn(bodySpec);
        lenient().when(bodySpec.retrieve()).thenReturn(responseSpec);
        lenient().when(responseSpec.toBodilessEntity()).thenReturn(ResponseEntity.ok().build());
    }


    @Test
    void prescoring_shouldReturnOffers() throws Exception {
        List<LoanOfferDto> offers = List.of(createOffer(), createOffer(), createOffer(), createOffer());
        when(responseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(offers);

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
        lenient().when(responseSpec.body(any(ParameterizedTypeReference.class)))
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
        lenient().when(responseSpec.toBodilessEntity())
                .thenThrow(new RuntimeException("Statement service unavailable"));

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
