package ru.neoflex.gateway.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import ru.neoflex.gateway.dto.LoanOfferDto;
import ru.neoflex.gateway.dto.LoanStatementRequestDto;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class StatementGatewayClient {

    private final RestClient statementRestClient;

    public List<LoanOfferDto> createStatement(LoanStatementRequestDto request) {
        return statementRestClient.post()
                .uri("/statement")
                .body(request)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    public void selectOffer(LoanOfferDto request) {
        statementRestClient.post()
                .uri("/statement/offer")
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }
}
