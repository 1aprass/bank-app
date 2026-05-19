package ru.neoflex.gateway.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import ru.neoflex.gateway.dto.FinishRegistrationRequestDto;
import ru.neoflex.gateway.dto.StatementDto;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DealGatewayClient {

    private final RestClient dealRestClient;

    public void finishRegistration(FinishRegistrationRequestDto request, String statementId) {
        dealRestClient.post()
                .uri("/deal/calculate/{statementId}", statementId)
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }

    public void sendDocuments(String statementId) {
        dealRestClient.post()
                .uri("/deal/document/{statementId}/send", statementId)
                .retrieve()
                .toBodilessEntity();
    }

    public void requestSignDocuments(String statementId) {
        dealRestClient.post()
                .uri("/deal/document/{statementId}/sign", statementId)
                .retrieve()
                .toBodilessEntity();
    }

    public void signDocuments(String statementId, String code) {
        dealRestClient.post()
                .uri("/deal/document/{statementId}/code?code={code}", statementId, code)
                .retrieve()
                .toBodilessEntity();
    }

    public StatementDto getStatementById(String statementId) {
        return dealRestClient.get()
                .uri("/deal/admin/statement/{statementId}", statementId)
                .retrieve()
                .body(StatementDto.class);
    }

    public List<StatementDto> getAllStatements() {
        return dealRestClient.get()
                .uri("/deal/admin/statement")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }
}
