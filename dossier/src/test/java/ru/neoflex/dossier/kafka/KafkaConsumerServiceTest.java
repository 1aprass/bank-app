package ru.neoflex.dossier.kafka;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.neoflex.dossier.dto.EmailMessageDto;
import ru.neoflex.dossier.enums.ThemeEnum;
import ru.neoflex.dossier.mapper.EmailMessageMapper;
import ru.neoflex.dossier.service.EmailService;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KafkaConsumerServiceTest {

    @Mock
    private EmailService emailService;

    @Mock
    private EmailMessageMapper emailMessageMapper;

    @InjectMocks
    private KafkaConsumerService kafkaConsumerService;

    @Test
    void handleFinishRegistration_shouldCallEmailServiceWithCorrectDto() {
        String statementId = UUID.randomUUID().toString();

        Map<String, Object> message = new HashMap<>();
        message.put("address", "user@example.com");
        message.put("theme", "FINISH_REGISTRATION");
        message.put("statementId", statementId);
        message.put("text", "Complete registration");

        EmailMessageDto expectedDto = new EmailMessageDto();
        expectedDto.setAddress("user@example.com");
        expectedDto.setTheme(ThemeEnum.FINISH_REGISTRATION);
        expectedDto.setStatementId(statementId);
        expectedDto.setText("Complete registration");

        when(emailMessageMapper.toDto(message)).thenReturn(expectedDto);
        kafkaConsumerService.handleFinishRegistration(message);
        verify(emailMessageMapper, times(1)).toDto(message);
        verify(emailService, times(1)).sendEmail(expectedDto);
    }

    @Test
    void createDocuments_shouldCallEmailServiceWithCorrectDto() {
        String statementId = UUID.randomUUID().toString();

        Map<String, Object> message = new HashMap<>();
        message.put("address", "user@example.com");
        message.put("theme", "CREATE_DOCUMENTS");
        message.put("statementId", statementId);
        message.put("text", "Create documents");

        EmailMessageDto expectedDto = new EmailMessageDto();
        expectedDto.setAddress("user@example.com");
        expectedDto.setTheme(ThemeEnum.CREATE_DOCUMENTS);
        expectedDto.setStatementId(statementId);
        expectedDto.setText("Create documents");

        when(emailMessageMapper.toDto(message)).thenReturn(expectedDto);
        kafkaConsumerService.createDocuments(message);
        verify(emailService, times(1)).sendEmail(expectedDto);
    }

    @Test
    void sendDocuments_shouldCallEmailServiceWithCorrectDto() {
        String statementId = UUID.randomUUID().toString();

        Map<String, Object> message = new HashMap<>();
        message.put("theme", "SEND_DOCUMENTS");
        message.put("text", "Documents ready");
        message.put("address", "user@example.com");
        message.put("statementId", statementId);


        EmailMessageDto expectedDto = new EmailMessageDto();
        expectedDto.setTheme(ThemeEnum.SEND_DOCUMENTS);
        expectedDto.setText("Documents ready");
        expectedDto.setAddress("user@example.com");
        expectedDto.setStatementId(statementId);

        when(emailMessageMapper.toDto(message)).thenReturn(expectedDto);
        kafkaConsumerService.sendDocuments(message);
        verify(emailService, times(1)).sendEmail(expectedDto);
    }

    @Test
    void sendSes_shouldCallEmailServiceWithCorrectDto() {
        String statementId = UUID.randomUUID().toString();

        Map<String, Object> message = new HashMap<>();
        message.put("theme", "SEND_SES");
        message.put("text", "Your code: 1234");
        message.put("address", "user@example.com");
        message.put("statementId", statementId);

        EmailMessageDto expectedDto = new EmailMessageDto();
        expectedDto.setTheme(ThemeEnum.SEND_SES);
        expectedDto.setText("Your code: 1234");
        expectedDto.setAddress("user@example.com");
        expectedDto.setStatementId(statementId);

        when(emailMessageMapper.toDto(message)).thenReturn(expectedDto);
        kafkaConsumerService.sendSes(message);
        verify(emailService, times(1)).sendEmail(expectedDto);
    }

    @Test
    void creditIssued_shouldCallEmailServiceWithCorrectDto() {
        String statementId = UUID.randomUUID().toString();

        Map<String, Object> message = new HashMap<>();
        message.put("theme", "CREDIT_ISSUED");
        message.put("text", "Credit issued");
        message.put("address", "user@example.com");
        message.put("statementId", statementId);

        EmailMessageDto expectedDto = new EmailMessageDto();
        expectedDto.setTheme(ThemeEnum.CREDIT_ISSUED);
        expectedDto.setText("Credit issued");
        expectedDto.setAddress("user@example.com");
        expectedDto.setStatementId(statementId);

        when(emailMessageMapper.toDto(message)).thenReturn(expectedDto);
        kafkaConsumerService.creditIssued(message);
        verify(emailService, times(1)).sendEmail(expectedDto);
    }

    @Test
    void statementDenied_shouldCallEmailServiceWithCorrectDto() {
        String statementId = UUID.randomUUID().toString();

        Map<String, Object> message = new HashMap<>();
        message.put("theme", "STATEMENT_DENIED");
        message.put("text", "Loan denied");
        message.put("address", "user@example.com");
        message.put("statementId", statementId);

        EmailMessageDto expectedDto = new EmailMessageDto();
        expectedDto.setTheme(ThemeEnum.STATEMENT_DENIED);
        expectedDto.setText("Loan denied");
        expectedDto.setAddress("user@example.com");
        expectedDto.setStatementId(statementId);

        when(emailMessageMapper.toDto(message)).thenReturn(expectedDto);
        kafkaConsumerService.statementDenied(message);
        verify(emailService, times(1)).sendEmail(expectedDto);
    }

    @Test
    void handleMessage_withNullDto_shouldNotCallEmailService() {
        Map<String, Object> message = new HashMap<>();
        when(emailMessageMapper.toDto(message)).thenReturn(null);

        kafkaConsumerService.handleFinishRegistration(message);
        verify(emailService, never()).sendEmail(any());
    }

}
