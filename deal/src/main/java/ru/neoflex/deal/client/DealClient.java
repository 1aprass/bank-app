package ru.neoflex.deal.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import ru.neoflex.deal.dto.CreditDto;
import ru.neoflex.deal.dto.LoanOfferDto;
import ru.neoflex.deal.dto.LoanStatementRequestDto;
import ru.neoflex.deal.dto.ScoringDataDto;

import java.util.List;

@Slf4j
@Service
public class DealClient {

    private final RestClient client;

    public DealClient(@Value("${deal.service.base-url}") String baseUrl) {
        this.client = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public List<LoanOfferDto> createStatement(LoanStatementRequestDto request) {
        try {
            return client.post()
                    .uri("/calculator/offers")
                    .body(request)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<LoanOfferDto>>() {});
        } catch (Exception e) {
            log.error("Error creating loan statement", e);
            throw e;
        }
    }

    public CreditDto getFinishRegistration(ScoringDataDto scoringDataDto) {
        try {
            return client.post()
                    .uri("/calculator/calc")
                    .body(scoringDataDto)
                    .retrieve()
                    .body(CreditDto.class);
        } catch (Exception e) {
            log.error("Error finishing registration", e);
            throw e;
        }
    }
}
