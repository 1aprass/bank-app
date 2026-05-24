package ru.neoflex.deal.Service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.neoflex.deal.config.AppConfig;
import ru.neoflex.deal.config.KafkaTopicsConfig;
import ru.neoflex.deal.dto.*;
import ru.neoflex.deal.entity.*;
import ru.neoflex.deal.client.DealClient;
import ru.neoflex.deal.enums.ApplicationStatus;
import ru.neoflex.deal.enums.Gender;
import ru.neoflex.deal.enums.MaritalStatus;
import ru.neoflex.deal.kafka.KafkaProducerService;
import ru.neoflex.deal.mapper.*;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DealServiceImplFinishRegistrationTest {
    @Mock
    private StatementRepository statementRepository;
    @Mock
    private ClientRepository clientRepository;
    @Mock
    private CreditRepository creditRepository;
    @Mock
    private EmploymentRepository employmentRepository;
    @Mock
    private DealClient dealClient;
    @Mock
    private CreditMapper creditMapper;
    @Mock
    private EmploymentMapper employmentMapper;
    @Mock
    private PassportRepository passportRepository;
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

    private Statement statement;
    private UUID statementId;
    private Client client;
    private Passport passport;
    private LoanOfferDto loanOfferDto;

    @BeforeEach
    void setUp() {
        statementId = UUID.randomUUID();

        client = new Client();
        client.setFirstName("Ivan");
        client.setLastName("Ivanov");
        client.setBirthDate(LocalDate.of(1990, 1, 1));
        passport = new Passport();
        passport.setSeries("1234");
        passport.setNumber("567890");
        client.setPassport(passport);

        statement = new Statement();
        statement.setStatementId(statementId);
        statement.setClient(client);
        statement.setAppliedOffer(new LoanOfferDto());
        statement.setApplicationStatus(ApplicationStatus.PREAPPROVAL);
        statement.setStatusHistory(new ArrayList<>());

        loanOfferDto = new LoanOfferDto();
        loanOfferDto.setStatementId(statementId);
        loanOfferDto.setRequestedAmount(BigDecimal.valueOf(100_000));
        loanOfferDto.setTotalAmount(BigDecimal.valueOf(110_000));
        loanOfferDto.setTerm(12);
        loanOfferDto.setMonthlyPayment(BigDecimal.valueOf(9166.67));
        loanOfferDto.setRate(BigDecimal.valueOf(15));
        loanOfferDto.setIsInsuranceEnabled(true);
        loanOfferDto.setIsSalaryClient(false);

        lenient().when(appConfig.getPublicUrl()).thenReturn("http://localhost:8081/deal/document/");
    }

    @Test
    void finishRegistration_shouldUpdateStatementClientCredit() {
        FinishRegistrationRequestDto requestDto = new FinishRegistrationRequestDto();
        requestDto.setGender(Gender.MALE);
        requestDto.setMaritalStatus(MaritalStatus.SINGLE);
        requestDto.setDependentAmount(0);
        requestDto.setAccountNumber("1234567890");
        requestDto.setPassportIssueDate(LocalDate.of(2020, 1, 1));
        requestDto.setPassportIssueBranch("Test Branch");
        requestDto.setEmployment(new EmploymentDto());

        CreditDto creditDto = new CreditDto();
        creditDto.setAmount(BigDecimal.valueOf(100_000));
        creditDto.setTerm(12);
        creditDto.setMonthlyPayment(BigDecimal.valueOf(9166.67));
        creditDto.setRate(BigDecimal.valueOf(15));
        creditDto.setIsInsuranceEnabled(true);
        creditDto.setIsSalaryClient(false);
        creditDto.setPaymentSchedule(List.of());

        Credit creditEntity = new Credit();

        Employment employmentEntity = new Employment();

        when(statementRepository.findById(statementId)).thenReturn(Optional.of(statement));
        when(dealClient.getFinishRegistration(any())).thenReturn(creditDto);
        when(creditMapper.toEntity(creditDto)).thenReturn(creditEntity);
        when(employmentMapper.toEntity(any(EmploymentDto.class))).thenReturn(employmentEntity);
        when(creditRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(employmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(clientRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(statementRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        dealService.finishRegistration(requestDto, statementId.toString());

        assertEquals(ApplicationStatus.CC_APPROVED, statement.getApplicationStatus());
        assertEquals(creditEntity, statement.getCredit());
        assertEquals(1, statement.getStatusHistory().size());
        assertEquals(ApplicationStatus.CC_APPROVED, statement.getStatusHistory().get(0).getStatus());

        assertEquals(Gender.MALE, client.getGender());
        assertEquals(MaritalStatus.SINGLE, client.getMaritalStatus());
        assertEquals(0, client.getDependentAmount());
        assertEquals("1234567890", client.getAccountNumber());

        assertEquals("Test Branch", passport.getIssueBranch());
        assertEquals(LocalDate.of(2020, 1, 1), passport.getIssueDate());
        assertEquals(employmentEntity, client.getEmployment());

        verify(creditRepository).save(creditEntity);
        verify(employmentRepository).save(employmentEntity);
        verify(clientRepository).save(client);
        verify(statementRepository).save(statement);
    }

    @Test
    void finishRegistration_shouldThrow_ifStatementNotFound() {
        FinishRegistrationRequestDto requestDto = new FinishRegistrationRequestDto();
        when(statementRepository.findById(statementId)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> dealService.finishRegistration(requestDto, statementId.toString()));
        assertTrue(ex.getMessage().contains("Statement not found with id"));
    }

    @Test
    void finishRegistration_shouldThrow_ifPassportNotFound() {
        statement.setClient(new Client());
        when(statementRepository.findById(statementId)).thenReturn(Optional.of(statement));

        FinishRegistrationRequestDto requestDto = new FinishRegistrationRequestDto();
        requestDto.setEmployment(new EmploymentDto());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> dealService.finishRegistration(requestDto, statementId.toString()));

        assertTrue(ex.getMessage().contains("Passport not found for client"));
    }

    @Test
    void finishRegistration_shouldAddStatusHistoryToExisting() {
        statement.getStatusHistory().add(new StatementStatusHistoryDto());

        FinishRegistrationRequestDto requestDto = new FinishRegistrationRequestDto();
        requestDto.setGender(Gender.MALE);
        requestDto.setMaritalStatus(MaritalStatus.SINGLE);
        requestDto.setDependentAmount(0);
        requestDto.setAccountNumber("1234567890");
        requestDto.setPassportIssueDate(LocalDate.of(2020, 1, 1));
        requestDto.setPassportIssueBranch("Branch");
        requestDto.setEmployment(new EmploymentDto());

        CreditDto creditDto = new CreditDto();
        creditDto.setIsInsuranceEnabled(true);
        creditDto.setIsSalaryClient(false);
        creditDto.setPaymentSchedule(List.of());
        when(statementRepository.findById(statementId)).thenReturn(Optional.of(statement));
        when(dealClient.getFinishRegistration(any())).thenReturn(creditDto);
        when(creditMapper.toEntity(any())).thenReturn(new Credit());
        when(employmentMapper.toEntity(any())).thenReturn(new Employment());
        when(creditRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(employmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(clientRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(statementRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        dealService.finishRegistration(requestDto, statementId.toString());

        assertEquals(2, statement.getStatusHistory().size());
        assertEquals(ApplicationStatus.CC_APPROVED, statement.getStatusHistory().get(1).getStatus());
    }

    @Test
    void finishRegistration_shouldThrow_ifClientIsNull() {
        statement.setClient(null);
        when(statementRepository.findById(statementId)).thenReturn(Optional.of(statement));

        FinishRegistrationRequestDto requestDto = new FinishRegistrationRequestDto();

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> dealService.finishRegistration(requestDto, statementId.toString()));
        assertTrue(ex.getMessage().contains("Client not found for statement"));
    }

    @Test
    void finishRegistration_shouldThrow_ifPassportIsNull() {
        Client clientWithoutPassport = new Client();
        statement.setClient(clientWithoutPassport);
        when(statementRepository.findById(statementId)).thenReturn(Optional.of(statement));

        FinishRegistrationRequestDto requestDto = new FinishRegistrationRequestDto();
        requestDto.setEmployment(new EmploymentDto());
        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> dealService.finishRegistration(requestDto, statementId.toString()));
        assertTrue(ex.getMessage().contains("Passport not found for client"));
    }

    @Test
    void finishRegistration_shouldThrow_ifAppliedOfferIsNull() {
        Client clientWithPassport = new Client();
        Passport passport = new Passport();
        clientWithPassport.setPassport(passport);
        statement.setClient(clientWithPassport);
        statement.setAppliedOffer(null);
        when(statementRepository.findById(statementId)).thenReturn(Optional.of(statement));

        FinishRegistrationRequestDto requestDto = new FinishRegistrationRequestDto();
        requestDto.setEmployment(new EmploymentDto());

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> dealService.finishRegistration(requestDto, statementId.toString()));
        assertTrue(ex.getMessage().contains("Applied offer not found for statement"));
    }
    @Test
    void finishRegistration_invalidUUID_shouldThrowException() {
        String invalidUuid = "not-a-uuid";
        FinishRegistrationRequestDto requestDto = new FinishRegistrationRequestDto();
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                dealService.finishRegistration(requestDto, invalidUuid)
        );
        assert exception.getMessage().contains("Invalid statementId format");
        verify(statementRepository, never()).findById(any(UUID.class));
    }


}
