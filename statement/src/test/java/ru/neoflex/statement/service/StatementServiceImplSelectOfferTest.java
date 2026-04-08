package ru.neoflex.statement.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.neoflex.statement.client.StatementClient;
import ru.neoflex.statement.controller.StatementController;
import ru.neoflex.statement.dto.LoanOfferDto;
import ru.neoflex.statement.service.impl.StatementServiceImpl;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class StatementServiceImplSelectOfferTest {
    @Mock
    private StatementController statementController;

    @Mock
    private StatementClient statementClient;

    @InjectMocks
    private StatementServiceImpl statementService;

    @Test
    void selectOffer_shouldCallClient() {
        LoanOfferDto dto = new LoanOfferDto();

        statementService.selectOffer(dto);

        verify(statementClient).selectOffer(dto);
    }

    @Test
    void selectOffer_shouldThrowException_whenClientFails() {
        LoanOfferDto dto = new LoanOfferDto();

        doThrow(new RuntimeException())
                .when(statementClient).selectOffer(dto);

        assertThrows(RuntimeException.class, () -> statementService.selectOffer(dto));

        verify(statementClient).selectOffer(dto);
    }
}
