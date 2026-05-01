package ru.neoflex.dossier.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import ru.neoflex.dossier.dto.EmailMessageDto;
import ru.neoflex.dossier.mapper.EmailMessageMapper;
import ru.neoflex.dossier.service.EmailService;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumerService {
    private final EmailService emailService;
    private final EmailMessageMapper emailMessageMapper;

    @KafkaListener(topics = "finish-registration", groupId = "dossier-group")
    public void handleFinishRegistration(Map<String, Object> message) {
        log.info("Received FINISH_REGISTRATION {}", message);
        EmailMessageDto dto = emailMessageMapper.toDto(message);
        if (validDto(dto)) {
            emailService.sendEmail(dto);
        }
    }

    @KafkaListener(topics = "create-documents", groupId = "dossier-group")
    public void createDocuments(Map<String, Object> message) {
        log.info("Received CREATE_DOCUMENTS {}", message);
        EmailMessageDto dto = emailMessageMapper.toDto(message);
        if (validDto(dto)) {
            emailService.sendEmail(dto);
        }
    }

    @KafkaListener(topics = "send-documents", groupId = "dossier-group")
    public void sendDocuments(Map<String, Object> message) {
        log.info("Received SEND_DOCUMENTS {}", message);
        EmailMessageDto dto = emailMessageMapper.toDto(message);
        if (validDto(dto)) {
            emailService.sendEmail(dto);
        }
    }

    @KafkaListener(topics = "send-ses", groupId = "dossier-group")
    public void sendSes(Map<String, Object> message) {
        log.info("Received SEND_SES {}", message);
        EmailMessageDto dto = emailMessageMapper.toDto(message);
        if (validDto(dto)) {
            emailService.sendEmail(dto);
        }
    }

    @KafkaListener(topics = "credit-issued", groupId = "dossier-group")
    public void creditIssued(Map<String, Object> message) {
        log.info("Received CREDIT_ISSUED {}", message);
        EmailMessageDto dto = emailMessageMapper.toDto(message);
        if (validDto(dto)) {
            emailService.sendEmail(dto);
        }
    }

    @KafkaListener(topics = "statement-denied", groupId = "dossier-group")
    public void statementDenied(Map<String, Object> message) {
        log.info("Received STATEMENT_DENIED {}", message);
        EmailMessageDto dto = emailMessageMapper.toDto(message);
        if (validDto(dto)) {
            emailService.sendEmail(dto);
        }
    }

    public boolean validDto(EmailMessageDto dto){
        if(dto == null) return false;
        boolean valid = true;

        if(dto.getAddress() == null || dto.getAddress().isBlank()){
            log.error("KafkaConsumerService. Email address is null or empty");
            valid = false;
        }

        if(dto.getText() == null || dto.getText().isBlank()){
            log.error("KafkaConsumerService. Text for message is null or empty");
            valid = false;
        }

        if(dto.getStatementId() == null || dto.getStatementId().isBlank()){
            log.error("KafkaConsumerService. Statement id is null or empty");
            valid = false;
        }

        if (dto.getTheme() == null) {
            log.error("KafkaConsumerService. Email theme is null");
            valid = false;
        }
        return valid;
    }

}
