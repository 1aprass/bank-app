package ru.neoflex.statement.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.neoflex.statement.client.StatementClient;
import ru.neoflex.statement.controller.StatementController;
import ru.neoflex.statement.dto.LoanOfferDto;
import ru.neoflex.statement.dto.LoanStatementRequestDto;
import ru.neoflex.statement.service.impl.StatementServiceImpl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class StatementServiceImplPrescoringTest {
    @Mock
    private StatementController statementController;

    @Mock
    private StatementClient statementClient;

    @InjectMocks
    private StatementServiceImpl statementService;

    @Test
    void prescoring_shouldCreateAllEntities_andReturnOffers() {
        LoanStatementRequestDto request = createValidLoanStatementRequest();
        LoanOfferDto offer = new LoanOfferDto();
        when(statementClient.prescoring(any())).thenReturn(List.of(offer));

        List<LoanOfferDto> response = statementService.prescoring(request);

        assertEquals(1, response.size());
        verify(statementClient).prescoring(request);
    }

    @Test
    void prescoring_shouldReturnEmptyList(){
        LoanStatementRequestDto request = createValidLoanStatementRequest();
        when(statementClient.prescoring(request)).thenReturn(List.of());

        List<LoanOfferDto> result = statementService.prescoring(request);
        assertTrue(result.isEmpty());
        verify(statementClient).prescoring(request);
    }

    @Test
    void prescoring_shouldThrowException_whenClientFails(){
        LoanStatementRequestDto request = createValidLoanStatementRequest();
        when(statementClient.prescoring(request)).thenThrow(new RuntimeException());
        assertThrows(RuntimeException.class, () -> statementService.prescoring(request));
        verify(statementClient).prescoring(request);
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
