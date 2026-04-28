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
import ru.neoflex.deal.enums.ThemeEnum;
import ru.neoflex.deal.exception.ScoringDeniedException;
import ru.neoflex.deal.kafka.KafkaProducerService;
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
    private final KafkaProducerService kafkaProducer;

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

        UUID statementId = request.getStatementId();

        Statement statement = statementRepository.findByIdWithLock(statementId)
                .orElseThrow(() -> {log.error("Statement not found with id: {}", statementId);
                    return new EntityNotFoundException("Statement not found: " + statementId);});

        statement.setApplicationStatus(ApplicationStatus.APPROVED);
        statement.setAppliedOffer(request);

        addStatusHistory(statement, ApplicationStatus.APPROVED, ChangeType.MANUAL);
        log.info("selectOffer. Output - offer selected and statement {} updated to APPROVED", statement.getStatementId());

        EmailMessageDto emailMessageDto = new EmailMessageDto();
        emailMessageDto.setStatementId(statementId.toString());
        emailMessageDto.setAddress(statement.getClient().getEmail());
        emailMessageDto.setText("Complete the registration");
        emailMessageDto.setTheme(ThemeEnum.FINISH_REGISTRATION);
        kafkaProducer.sendMessage("finish-registration", emailMessageDto);

        log.info("selectOffer. Output - offer was sent to kafka {}", emailMessageDto);
        statementRepository.save(statement);
    }

    @Transactional
    @Override
    public void finishRegistration(FinishRegistrationRequestDto requestDto, String statementId) {
        log.info("finishRegistration. Input - Start finishRegistration for statementId={}", statementId);
        log.debug("FinishRegistrationRequestDto: {}", requestDto);

        UUID statementID;
        try {
            statementID = UUID.fromString(statementId);
        } catch (IllegalArgumentException ex){
            log.error("Invalid UUID format for statementId={}", statementId, ex);
            throw new IllegalArgumentException("Invalid statementId format: " + statementId);
        }
        Statement statement = statementRepository.findById(statementID)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Statement not found with id: " + statementID));

        ScoringDataDto scoringDataDto = new ScoringDataDto();
        createScoringDataDto(scoringDataDto, statement, requestDto);
        log.debug("finishRegistration. ScoringDataDto created: {}", scoringDataDto);

        CreditDto creditDto = null;
        try {
            creditDto = dealClient.getFinishRegistration(scoringDataDto);
        } catch (Exception e){
            log.error("Scoring failed for statementId={}: {}", statementID, e.getMessage());
            statement.setApplicationStatus(ApplicationStatus.CC_DENIED);
            addStatusHistory(statement, ApplicationStatus.CC_DENIED, ChangeType.AUTOMATIC);

            EmailMessageDto message = new EmailMessageDto();
            message.setStatementId(statementID.toString());
            message.setAddress(statement.getClient().getEmail());
            message.setTheme(ThemeEnum.STATEMENT_DENIED);
            message.setText("Your loan application was denied");
            kafkaProducer.sendMessage("statement-denied", message);

            statementRepository.save(statement);
            log.info("finishRegistration. Statement {} moved to CC_DENIED", statementID);
            throw new ScoringDeniedException(e.getMessage(), e);
        }
        log.info("finishRegistration. Output - Received CreditDto from calculator for statementID={}", statementID);
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

        EmploymentDto employmentDto = requestDto.getEmployment();
        Employment employment = employmentMapper.toEntity(employmentDto);

        employmentRepository.save(employment);
        client.setEmployment(employment);

        clientRepository.save(client);
        log.debug("finishRegistration. Client updated with client={}", client);
        log.debug("finishRegistration. Employment saved with employment={}", employment);

        String documentsLink = "http://localhost:8084/deal/document/" + statementID + "/send";
        EmailMessageDto message = new EmailMessageDto();
        message.setStatementId(statementID.toString());
        message.setAddress(statement.getClient().getEmail());
        message.setTheme(ThemeEnum.CREATE_DOCUMENTS);
        message.setText(String.format(
                "Your loan is approved.\n\n" +
                        "To create documents, follow the link:\n" +
                        "%s",
                documentsLink
        ));
        kafkaProducer.sendMessage("create-documents", message);


        log.info("finishRegistration. Kafka message sent {}", message);

        statementRepository.save(statement);
        log.info("finishRegistration. Output - finishRegistration completed, statement {} moved to CC_APPROVED", statementID);
    }

    private void addStatusHistory(Statement statement,
                                  ApplicationStatus status,
                                  ChangeType changeType) {

        StatementStatusHistoryDto history = new StatementStatusHistoryDto();
        history.setStatus(status);
        history.setTime(LocalDateTime.now());
        history.setChangeType(changeType);

        List<StatementStatusHistoryDto> historyList = Optional
                .ofNullable(statement.getStatusHistory())
                .orElse(new ArrayList<>());

        historyList.add(history);
        statement.setStatusHistory(historyList);
    }

    @Transactional
    @Override
    public void sendDocuments(String statementId) {
        log.info("Input - sendDocuments(). statementId {}", statementId);
        UUID id = UUID.fromString(statementId);

        Statement statement = statementRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Statement not found"));

        statement.setApplicationStatus(ApplicationStatus.PREPARE_DOCUMENTS);
        addStatusHistory(statement, ApplicationStatus.PREPARE_DOCUMENTS, ChangeType.MANUAL);

        String signLink = "http://localhost:8084/deal/document/" + statementId + "/sign";
        EmailMessageDto message = new EmailMessageDto();
        message.setStatementId(statementId);
        message.setAddress(statement.getClient().getEmail());
        message.setTheme(ThemeEnum.SEND_DOCUMENTS);
        message.setText(String.format(
                "Documents are prepared and sent to your email.\n\n" +
                        "To agree with the conditions, follow the link:\n" +
                        "%s",
                signLink
        ));
        kafkaProducer.sendMessage("send-documents", message);
        log.debug("sendDocuments(). massage was sent to kafka");

        statementRepository.save(statement);

        log.info("sendDocuments. Documents sent for {}", statementId);

    }

    @Transactional
    @Override
    public void requestSignDocuments(String statementId) {
        log.info("Input - requestSignDocuments(). statementId {}", statementId);
        UUID id = UUID.fromString(statementId);

        Statement statement = statementRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Statement not found"));

        String code = String.valueOf((int)(Math.random() * 9000) + 1000);
        statement.setSesCode(code);

        statement.setApplicationStatus(ApplicationStatus.DOCUMENT_CREATED);
        addStatusHistory(statement, ApplicationStatus.DOCUMENT_CREATED, ChangeType.MANUAL);

        String verifyCodeLink = "http://localhost:8084/deal/document/" + statementId + "/code?code=" + code;
        EmailMessageDto message = new EmailMessageDto();
        message.setStatementId(statementId);
        message.setAddress(statement.getClient().getEmail());
        message.setTheme(ThemeEnum.SEND_SES);
        message.setText(String.format(
                "Your confirmation code: %s\n\n" +
                        "To sign the documents, follow the link:\n" +
                        "%s\n\n",
                code, verifyCodeLink
        ));
        kafkaProducer.sendMessage("send-ses", message);
        log.debug("requestSignDocuments(). massage was sent to kafka");

        statementRepository.save(statement);

        log.info("Output - requestSignDocuments(). SES code sent {}", code);
    }

    @Transactional
    @Override
    public void signDocuments(String statementId, String sesCode) {
        log.info("Input - signDocuments(). statementId {} and sesCode {}", statementId, sesCode);
        UUID id = UUID.fromString(statementId);

        Statement statement = statementRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Statement not found"));

        if (statement.getSesCode() == null) {
            log.error("signDocuments(). No SES code generated for statement {}", statementId);
            throw new IllegalStateException("No code was sent to client");
        }

        if (!statement.getSesCode().equals(sesCode)) {
            log.error("signDocuments(). Invalid SES code for statement {}: expected {}, got {}",
                    statementId, statement.getSesCode(), sesCode);
            throw new IllegalArgumentException("Invalid SES code");
        }

        log.info("signDocuments(). SES code verified successfully for statement {}", statementId);

        statement.setApplicationStatus(ApplicationStatus.DOCUMENT_SIGNED);
        addStatusHistory(statement, ApplicationStatus.DOCUMENT_SIGNED, ChangeType.MANUAL);
        log.info("signDocuments(). Statement was set with ApplicationStatus {}", statement.getApplicationStatus());

        statement.setApplicationStatus(ApplicationStatus.CREDIT_ISSUED);
        addStatusHistory(statement, ApplicationStatus.CREDIT_ISSUED, ChangeType.AUTOMATIC);
        log.info("signDocuments(). Statement was set with ApplicationStatus {}", statement.getApplicationStatus());

        Credit credit = statement.getCredit();
        credit.setCreditStatus(CreditStatus.ISSUED);

        creditRepository.save(credit);

        EmailMessageDto message = new EmailMessageDto();
        message.setStatementId(statementId);
        message.setAddress(statement.getClient().getEmail());
        message.setTheme(ThemeEnum.CREDIT_ISSUED);
        message.setText("Your credit has been issued!");
        kafkaProducer.sendMessage("credit-issued", message);
        log.debug("signDocuments(). massage was sent to kafka");

        statementRepository.save(statement);

        log.info("Output - signDocuments(). Credit issued for {}", statementId);

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
        scoringDataDto.setEmployment(requestDto.getEmployment());
        scoringDataDto.setAccountNumber(requestDto.getAccountNumber());
        scoringDataDto.setPassportSeries(passport.getSeries());
        scoringDataDto.setPassportNumber(passport.getNumber());
        scoringDataDto.setPassportIssueDate(requestDto.getPassportIssueDate());
        scoringDataDto.setPassportIssueBranch(requestDto.getPassportIssueBranch());
    }
}

