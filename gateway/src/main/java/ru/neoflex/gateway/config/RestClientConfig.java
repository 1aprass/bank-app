package ru.neoflex.gateway.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {
    @Value("${gateway.urls.statement-url}")
    private String statementUrl;

    @Value("${gateway.urls.deal-url}")
    private String dealUrl;

    @Bean
    @Qualifier("statementRestClient")
    public RestClient statementRestClient(){
        return RestClient.builder()
                .baseUrl(statementUrl)
                .build();
    }

    @Bean
    @Qualifier("dealRestClient")
    public RestClient dealRestClient(){
        return RestClient.builder()
                .baseUrl(dealUrl)
                .build();
    }
}
