package ru.neoflex.deal.Service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.neoflex.deal.client.DealClient;
import ru.neoflex.deal.dto.LoanOfferDto;
import ru.neoflex.deal.dto.LoanStatementRequestDto;
import ru.neoflex.deal.entity.Client;
import ru.neoflex.deal.entity.Passport;
import ru.neoflex.deal.entity.Statement;
import ru.neoflex.deal.enums.ApplicationStatus;
import ru.neoflex.deal.repository.ClientRepository;
import ru.neoflex.deal.repository.PassportRepository;
import ru.neoflex.deal.repository.StatementRepository;
import ru.neoflex.deal.service.impl.DealServiceImpl;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DealServiceImplCreateStatementTest {
    @Mock
    private ClientRepository clientRepository;
    @Mock
    private StatementRepository statementRepository;
    @Mock
    private PassportRepository passportRepository;
    @Mock
    private DealClient dealClient;

    @InjectMocks
    private DealServiceImpl dealService;

    @Test
    void createStatement_shouldCreateAllEntities_andReturnOffers() {
        LoanStatementRequestDto request = new LoanStatementRequestDto();
        request.setFirstName("Ivan");
        request.setLastName("Ivanov");
        request.setMiddleName("Ivanovich");
        request.setEmail("test@mail.com");
        request.setBirthdate(LocalDate.of(1990, 1, 1));
        request.setPassportSeries("1234");
        request.setPassportNumber("567890");

        when(clientRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(statementRepository.save(any())).thenAnswer(inv -> {
            Statement st = inv.getArgument(0);
            st.setStatementId(UUID.randomUUID());
            return st;
        });
        when(passportRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        LoanOfferDto offer = new LoanOfferDto();
        when(dealClient.createStatement(any())).thenReturn(List.of(offer));

        List<LoanOfferDto> result = dealService.createStatement(request);

        assertEquals(1, result.size());
        assertNotNull(result.get(0).getStatementId());

        verify(clientRepository).save(any(Client.class));
        verify(statementRepository).save(any(Statement.class));
        verify(passportRepository).save(any(Passport.class));
        verify(dealClient).createStatement(request);
    }

    @Test
    void createStatement_shouldAddStatusHistory() {
        when(clientRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(statementRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(passportRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(dealClient.createStatement(any())).thenReturn(List.of());

        dealService.createStatement(new LoanStatementRequestDto());

        ArgumentCaptor<Statement> captor = ArgumentCaptor.forClass(Statement.class);
        verify(statementRepository).save(captor.capture());

        Statement saved = captor.getValue();

        assertNotNull(saved.getStatusHistory());
        assertEquals(1, saved.getStatusHistory().size());
        assertEquals(ApplicationStatus.PREAPPROVAL,
                saved.getStatusHistory().get(0).getStatus());
    }

    @Test
    void createStatement_shouldCreatePassport_ifNotExists() {
        when(clientRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(statementRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(passportRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(dealClient.createStatement(any())).thenReturn(List.of());

        LoanStatementRequestDto request = new LoanStatementRequestDto();
        request.setPassportSeries("1111");
        request.setPassportNumber("222222");

        dealService.createStatement(request);

        verify(passportRepository).save(argThat(passport ->
                passport.getSeries().equals("1111") &&
                        passport.getNumber().equals("222222")
        ));
    }

    @Test
    void createStatement_shouldSetCorrectStatementId_inOffers() {
        when(clientRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UUID generatedStatementId = UUID.randomUUID();
        when(statementRepository.save(any())).thenAnswer(inv -> {
            Statement st = inv.getArgument(0);
            st.setStatementId(generatedStatementId);
            return st;
        });
        when(passportRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        LoanOfferDto offer = new LoanOfferDto();
        when(dealClient.createStatement(any())).thenReturn(List.of(offer));
        List<LoanOfferDto> result = dealService.createStatement(new LoanStatementRequestDto());

        assertEquals(generatedStatementId, result.get(0).getStatementId(),
                "StatementId в LoanOfferDto должен совпадать с Statement.statementId");
    }


}
