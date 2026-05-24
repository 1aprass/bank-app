package ru.neoflex.deal.Service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.neoflex.deal.client.DealClient;
import ru.neoflex.deal.config.AppConfig;
import ru.neoflex.deal.config.KafkaTopicsConfig;
import ru.neoflex.deal.dto.LoanOfferDto;
import ru.neoflex.deal.dto.StatementStatusHistoryDto;
import ru.neoflex.deal.entity.Client;
import ru.neoflex.deal.entity.Passport;
import ru.neoflex.deal.entity.Statement;
import ru.neoflex.deal.enums.ApplicationStatus;
import ru.neoflex.deal.enums.ChangeType;
import ru.neoflex.deal.kafka.KafkaProducerService;
import ru.neoflex.deal.mapper.CreditMapper;
import ru.neoflex.deal.mapper.EmploymentMapper;
import ru.neoflex.deal.mapper.StatementMapper;
import ru.neoflex.deal.repository.*;
import ru.neoflex.deal.service.impl.DealServiceImpl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DealServiceImplSelectOfferTest {
    @Mock
    private StatementRepository statementRepository;
    @Mock
    private ClientRepository clientRepository;
    @Mock
    private CreditRepository creditRepository;
    @Mock
    private EmploymentRepository employmentRepository;
    @Mock
    private PassportRepository passportRepository;
    @Mock
    private DealClient dealClient;
    @Mock
    private CreditMapper creditMapper;
    @Mock
    private EmploymentMapper employmentMapper;
    @Mock
    private StatementMapper statementMapper;
    @Mock
    private KafkaProducerService kafkaProducer;
    @Mock
    private KafkaTopicsConfig kafkaTopics;
    @Mock
    private AppConfig appConfig;

    @InjectMocks
    private DealServiceImpl dealService;

    @Test
    void selectOffer_shouldUpdateStatementAndAddStatusHistory() {
        UUID statementId = UUID.randomUUID();

        Client client = new Client();
        client.setFirstName("Ivan");
        client.setLastName("Ivanov");
        client.setBirthDate(LocalDate.of(1990, 1, 1));
        Passport passport = new Passport();
        passport.setSeries("1234");
        passport.setNumber("567890");
        client.setPassport(passport);

        Statement statement = new Statement();
        statement.setStatementId(statementId);
        statement.setClient(client);
        statement.setApplicationStatus(ApplicationStatus.PREAPPROVAL);
        statement.setStatusHistory(new ArrayList<>());

        LoanOfferDto request = new LoanOfferDto();
        request.setStatementId(statementId);
        request.setRequestedAmount(BigDecimal.valueOf(100_000));
        request.setTotalAmount(BigDecimal.valueOf(110_000));
        request.setTerm(12);
        request.setMonthlyPayment(BigDecimal.valueOf(9_166.67));
        request.setRate(BigDecimal.valueOf(15));
        request.setIsInsuranceEnabled(true);
        request.setIsSalaryClient(false);

        when(statementRepository.findByIdWithLock(statementId)).thenReturn(Optional.of(statement));
        when(statementRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        dealService.selectOffer(request);

        ArgumentCaptor<Statement> captor = ArgumentCaptor.forClass(Statement.class);
        verify(statementRepository).save(captor.capture());
        Statement saved = captor.getValue();

        assertEquals(ApplicationStatus.APPROVED, saved.getApplicationStatus());

        assertEquals(request, saved.getAppliedOffer());

        assertNotNull(saved.getStatusHistory());
        assertEquals(1, saved.getStatusHistory().size());
        assertEquals(ApplicationStatus.APPROVED, saved.getStatusHistory().get(0).getStatus());
        assertEquals(ChangeType.MANUAL, saved.getStatusHistory().get(0).getChangeType());
        assertNotNull(saved.getStatusHistory().get(0).getTime());
    }


    @Test
    void selectOffer_shouldThrow_ifStatementNotFound() {
        UUID statementId = UUID.randomUUID();
        LoanOfferDto request = new LoanOfferDto();
        request.setStatementId(statementId);

        when(statementRepository.findByIdWithLock(statementId)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> dealService.selectOffer(request));

        assertTrue(ex.getMessage().contains("Statement not found"));
    }

    @Test
    void selectOffer_shouldAddToExistingStatusHistory() {
        UUID statementId = UUID.randomUUID();

        Statement statement = new Statement();
        statement.setStatementId(statementId);
        statement.setClient(new Client());
        List history = new ArrayList<>();
        history.add(new StatementStatusHistoryDto());
        statement.setStatusHistory(history);

        LoanOfferDto request = new LoanOfferDto();
        request.setStatementId(statementId);
        request.setRequestedAmount(BigDecimal.valueOf(100_000));
        request.setTotalAmount(BigDecimal.valueOf(110_000));
        request.setTerm(12);
        request.setMonthlyPayment(BigDecimal.valueOf(9_166.67));
        request.setRate(BigDecimal.valueOf(15));
        request.setIsInsuranceEnabled(true);
        request.setIsSalaryClient(false);

        when(statementRepository.findByIdWithLock(statementId)).thenReturn(Optional.of(statement));
        when(statementRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        dealService.selectOffer(request);

        assertEquals(2, statement.getStatusHistory().size());
        assertEquals(ApplicationStatus.APPROVED, statement.getStatusHistory().get(1).getStatus());
    }
}
