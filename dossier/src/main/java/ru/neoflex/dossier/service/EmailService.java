package ru.neoflex.dossier.service;

import lombok.RequiredArgsConstructor;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import ru.neoflex.dossier.dto.EmailMessageDto;

@Service
@RequiredArgsConstructor
@Slf4j
@Setter
public class EmailService {

    @Value("${dossier.service}")
    private String fromEmail;

    private final JavaMailSender mailSender;

    public void sendEmail(EmailMessageDto message) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom(fromEmail);
        mail.setTo(message.getAddress());
        mail.setSubject(message.getTheme().name());
        mail.setText(message.getText());
        mailSender.send(mail);

        log.info("Email sent to {}", message.getAddress());
    }
}
