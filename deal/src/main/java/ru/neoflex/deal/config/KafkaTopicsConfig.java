package ru.neoflex.deal.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "deal.topics")
public class KafkaTopicsConfig {
    private String finishRegistration;
    private String createDocuments;
    private String sendDocuments;
    private String sendSes;
    private String creditIssued;
    private String statementDenied;
}