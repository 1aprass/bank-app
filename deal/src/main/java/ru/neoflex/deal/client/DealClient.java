package ru.neoflex.deal.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import ru.neoflex.deal.dto.CreditDto;
import ru.neoflex.deal.dto.LoanOfferDto;
import ru.neoflex.deal.dto.LoanStatementRequestDto;
import ru.neoflex.deal.dto.ScoringDataDto;

import java.util.List;

@Slf4j
@Service
public class DealClient {

    public List<LoanOfferDto> createStatement(LoanStatementRequestDto request) {

        WebClient client = WebClient.builder()
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .baseUrl("http://localhost:8080")
                .build();

        ResponseEntity<List<LoanOfferDto>> response = client.post()
                .uri("/calculator/offers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .toEntityList(LoanOfferDto.class)
                .block();

        return response.getBody();
    }

    public CreditDto getFinishRegistration(ScoringDataDto scoringDataDto) {

        WebClient client = WebClient.builder()
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .baseUrl("http://localhost:8080")
                .build();

        ResponseEntity<CreditDto> response = client.post()
                .uri("/calculator/calc")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(scoringDataDto)
                .retrieve()
                .toEntity(CreditDto.class)
                .block();

        return response.getBody();
    }
}
