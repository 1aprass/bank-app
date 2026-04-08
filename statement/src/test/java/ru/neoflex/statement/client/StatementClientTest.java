package ru.neoflex.statement.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;
import ru.neoflex.statement.dto.LoanOfferDto;
import ru.neoflex.statement.dto.LoanStatementRequestDto;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StatementClientTest {
    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestBodyUriSpec requestSpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    private StatementClient statementClient;

    @BeforeEach
    void setUp(){
        statementClient = new StatementClient("http://localhost:8081");
        ReflectionTestUtils.setField(statementClient, "client", restClient);
    }

    @Test
    void prescoringSuccess(){
        LoanStatementRequestDto request = new LoanStatementRequestDto();
        List<LoanOfferDto> response = List.of(new LoanOfferDto(), new LoanOfferDto(), new LoanOfferDto(), new LoanOfferDto());

        when(restClient.post()).thenReturn(requestSpec);
        when(requestSpec.uri("/deal/statement")).thenReturn(requestSpec);
        when(requestSpec.body(request)).thenReturn(requestSpec);
        when(requestSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(any(ParameterizedTypeReference.class))).thenReturn(response);

        List<LoanOfferDto> result = statementClient.prescoring(request);

        assertEquals(response, result);
    }

    @Test
    void prescoring_ShouldReturn_Error(){
        LoanStatementRequestDto request = new LoanStatementRequestDto();
        when(restClient.post()).thenThrow(new RuntimeException());

        assertThrows(RuntimeException.class, () -> statementClient.prescoring(request));
    }

    @Test
    void selectOfferSuccess(){
        LoanOfferDto request = new LoanOfferDto();

        when(restClient.post()).thenReturn(requestSpec);
        when(requestSpec.uri("/deal/offer/select")).thenReturn(requestSpec);
        when(requestSpec.body(request)).thenReturn(requestSpec);
        when(requestSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.toEntity(Void.class)).thenReturn(ResponseEntity.ok().build());

        statementClient.selectOffer(request);
        verify(restClient).post();

    }

    @Test
    void selectOffer_ShouldReturn_Error(){
        LoanOfferDto request = new LoanOfferDto();
        when(restClient.post()).thenThrow(new RuntimeException());

        assertThrows(RuntimeException.class, () -> statementClient.selectOffer(request));
    }
}
