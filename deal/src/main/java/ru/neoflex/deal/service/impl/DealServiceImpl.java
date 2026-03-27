package ru.neoflex.deal.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.neoflex.deal.client.DealClient;
import ru.neoflex.deal.dto.*;
import ru.neoflex.deal.entity.*;
import ru.neoflex.deal.enums.ApplicationStatus;
import ru.neoflex.deal.enums.ChangeType;
import ru.neoflex.deal.enums.CreditStatus;
import ru.neoflex.deal.mapper.CreditMapper;
import ru.neoflex.deal.mapper.EmploymentMapper;
import ru.neoflex.deal.repository.*;
import ru.neoflex.deal.service.DealService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class DealServiceImpl implements DealService {

    private final DealClient dealClient;
    private final ClientRepository clientRepository;
    private final StatementRepository statementRepository;
    private final CreditRepository creditRepository;
    private final EmploymentRepository employmentRepository;
    private final CreditMapper creditMapper;
    private final EmploymentMapper employmentMapper;
    private final PassportRepository passportRepository;

    @Transactional
    @Override
    public List<LoanOfferDto> createStatement(LoanStatementRequestDto request){
        log.info("createStatement. Input for LoanStatementRequestDto request={}", request);

        Client client = new Client();
        client.setFirstName(request.getFirstName());
        client.setBirthDate(request.getBirthdate());
        client.setLastName(request.getLastName());
        client.setMiddleName(request.getMiddleName());
        client.setEmail(request.getEmail());
        clientRepository.save(client);
        log.debug("createStatement - Client saved with client={}", client);

        Statement statement = new Statement();
        statement.setCreationDate(LocalDateTime.now());
        statement.setClient(client);
        statement.setApplicationStatus(ApplicationStatus.PREAPPROVAL);

        addStatusHistory(statement, ApplicationStatus.PREAPPROVAL, ChangeType.AUTOMATIC);

        statementRepository.save(statement);
        log.debug("createStatement - statement created with statement={}", statement);

        Passport passport = client.getPassport();

        if (passport == null) {
            passport = new Passport();
        }

        passport.setSeries(request.getPassportSeries());
        passport.setNumber(request.getPassportNumber());
        client.setPassport(passport);
        passportRepository.save(passport);


        List<LoanOfferDto> response;
        response = dealClient.createStatement(request);

        UUID statementId = statement.getStatementId();
        response.forEach(loanOfferDto -> loanOfferDto.setStatementId(statementId));
        log.debug("LoanOfferDto list: {}", response);
        log.info("createStatement. Output. Finished createStatement, offers count={}", response.size());
        return response;
    }

    @Transactional
    @Override
    public void selectOffer(LoanOfferDto request) {
        log.info("selectOffer. Input - Start selectOffer for statementId={}", request.getStatementId());
        log.debug("selectOffer. LoanOfferDto: {}", request);

        Statement statement = statementRepository.findById(request.getStatementId())
                .orElseThrow(() -> new EntityNotFoundException("Statement not found with id: " + request.getStatementId()));
        statement.setApplicationStatus(ApplicationStatus.APPROVED);
        statement.setAppliedOffer(request);

        addStatusHistory(statement, ApplicationStatus.APPROVED, ChangeType.MANUAL);
        log.info("selectOffer. Output - offer selected and statement {} updated to APPROVED", statement.getStatementId());
        statementRepository.save(statement);
    }

    @Transactional
    @Override
    public void finishRegistration(FinishRegistrationRequestDto requestDto, UUID statementId) {
        log.info("finishRegistration. Input - Start finishRegistration for statementId={}", statementId);
        log.debug("FinishRegistrationRequestDto: {}", requestDto);

        Statement statement = statementRepository.findById(statementId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Statement not found with id: " + statementId));

        ScoringDataDto scoringDataDto = new ScoringDataDto();
        createScoringDataDto(scoringDataDto, statement, requestDto);
        log.debug("finishRegistration. ScoringDataDto created: {}", scoringDataDto);

        CreditDto creditDto = dealClient.getFinishRegistration(scoringDataDto);
        log.info("finishRegistration. Output - Received CreditDto from calculator for statementId={}", statementId);
        log.debug("finishRegistration. CreditDto: {}", creditDto);

        Credit credit = creditMapper.toEntity(creditDto);
        credit.setCreditStatus(CreditStatus.CALCULATED);
        credit.setInsuranceEnabled(creditDto.getIsInsuranceEnabled());

        statement.setApplicationStatus(ApplicationStatus.CC_APPROVED);
        creditRepository.save(credit);
        log.debug("finishRegistration. Credit saved with credit={}", credit);

        statement.setCredit(credit);
        addStatusHistory(statement, ApplicationStatus.CC_APPROVED, ChangeType.AUTOMATIC);
        Client client = statement.getClient();
        client.setGender(requestDto.getGender());
        client.setMaritalStatus(requestDto.getMaritalStatus());
        client.setDependentAmount(requestDto.getDependentAmount());
        client.setAccountNumber(requestDto.getAccountNumber());

        Passport passport = client.getPassport();
        if (passport == null) {
            throw new EntityNotFoundException("Passport not found for client: " + client.getId());
        }
        passport.setIssueBranch(requestDto.getPassportIssueBranch());
        passport.setIssueDate(requestDto.getPassportIssueDate());

        EmploymentDto employmentDto = requestDto.getEmploymentDto();
        Employment employment = employmentMapper.toEntity(employmentDto);

        employmentRepository.save(employment);
        client.setEmployment(employment);

        clientRepository.save(client);
        log.debug("finishRegistration. Client updated with client={}", client);
        log.debug("finishRegistration. Employment saved with employment={}", employment);

        statementRepository.save(statement);
        log.info("finishRegistration. Output - finishRegistration completed, statement {} moved to CC_APPROVED", statementId);
    }

    private void addStatusHistory(Statement statement,
                                  ApplicationStatus status,
                                  ChangeType changeType) {

        StatusHistoryDto history = new StatusHistoryDto();
        history.setStatus(status);
        history.setTime(LocalDateTime.now());
        history.setChangeType(changeType);

        List<StatusHistoryDto> historyList = Optional
                .ofNullable(statement.getStatusHistoryDto())
                .orElse(new ArrayList<>());

        historyList.add(history);
        statement.setStatusHistoryDto(historyList);
    }

    private void createScoringDataDto(ScoringDataDto scoringDataDto,
                                      Statement statement,
                                      FinishRegistrationRequestDto requestDto){
        Client client = statement.getClient();
        if (client == null) {
            throw new EntityNotFoundException("Client not found for statement: " + statement.getStatementId());
        }

        Passport passport = client.getPassport();
        if (passport == null) {
            throw new EntityNotFoundException("Passport not found for client: " + client.getId());
        }

        LoanOfferDto offer = statement.getAppliedOffer();
        if (offer == null) {
            throw new IllegalStateException("Applied offer not found for statement: " + statement.getStatementId());
        }

        scoringDataDto.setAmount(offer.getRequestedAmount());
        scoringDataDto.setTerm(offer.getTerm());
        scoringDataDto.setIsInsuranceEnabled(offer.getIsInsuranceEnabled());
        scoringDataDto.setIsSalaryClient(offer.getIsSalaryClient());
        scoringDataDto.setFirstName(client.getFirstName());
        scoringDataDto.setLastName(client.getLastName());
        scoringDataDto.setMiddleName(client.getMiddleName());
        scoringDataDto.setBirthdate(client.getBirthDate());
        scoringDataDto.setGender(requestDto.getGender());
        scoringDataDto.setMaritalStatus(requestDto.getMaritalStatus());
        scoringDataDto.setDependentAmount(requestDto.getDependentAmount());
        scoringDataDto.setEmployment(requestDto.getEmploymentDto());
        scoringDataDto.setAccountNumber(requestDto.getAccountNumber());
        scoringDataDto.setPassportSeries(passport.getSeries());
        scoringDataDto.setPassportNumber(passport.getNumber());
        scoringDataDto.setPassportIssueDate(requestDto.getPassportIssueDate());
        scoringDataDto.setPassportIssueBranch(requestDto.getPassportIssueBranch());
    }
}

