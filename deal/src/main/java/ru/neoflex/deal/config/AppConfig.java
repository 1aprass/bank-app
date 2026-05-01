package ru.neoflex.deal.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "deal.app")
public class AppConfig {
    private String publicUrl;
}
