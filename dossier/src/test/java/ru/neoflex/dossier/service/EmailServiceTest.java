package ru.neoflex.dossier.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import ru.neoflex.dossier.dto.EmailMessageDto;
import ru.neoflex.dossier.enums.ThemeEnum;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmailServiceTest {
    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        emailService.setFromEmail("test@yandex.ru");
    }

    @Test
    void sendEmail_shouldSendWithCorrectSubjectAndBody() {
        EmailMessageDto dto = new EmailMessageDto();
        dto.setAddress("test@example.com");
        dto.setTheme(ThemeEnum.CREATE_DOCUMENTS);
        dto.setText("Your loan is approved");

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        emailService.sendEmail(dto);

        verify(mailSender, times(1)).send(messageCaptor.capture());
        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertThat(sentMessage.getTo()).containsExactly("test@example.com");
        assertThat(sentMessage.getSubject()).isEqualTo("CREATE_DOCUMENTS");
        assertThat(sentMessage.getText()).isEqualTo("Your loan is approved");
        assertThat(sentMessage.getFrom()).isEqualTo("test@yandex.ru");
    }

    @Test
    void sendEmail_shouldHandleAllThemes() {
        int expectedCalls = ThemeEnum.values().length;
        for (ThemeEnum theme : ThemeEnum.values()) {
            String statementId = UUID.randomUUID().toString();
            EmailMessageDto dto = new EmailMessageDto();
            dto.setAddress("test@example.com");
            dto.setTheme(theme);
            dto.setStatementId(statementId);
            dto.setText("Test message for " + theme);

            emailService.sendEmail(dto);
        }
        verify(mailSender, times(expectedCalls)).send(any(SimpleMailMessage.class));
    }
}
