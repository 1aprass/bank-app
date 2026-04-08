package ru.neoflex.statement.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import ru.neoflex.statement.dto.LoanOfferDto;
import ru.neoflex.statement.dto.LoanStatementRequestDto;

import java.util.List;

@Slf4j
@Service
public class StatementClient {
    private final RestClient client;

    public StatementClient(@Value("${statement.client.base-url}") String baseUrl){
        this.client = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public List<LoanOfferDto> prescoring(LoanStatementRequestDto request) {
        try{
            return client.post()
                    .uri("/deal/statement")
                    .body(request)
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<LoanOfferDto>>() {});
        }
        catch (Exception ex){
            log.error("Error creating loan statement", ex);
            throw ex;
        }
    }

    public void selectOffer(LoanOfferDto request){
        try{
            client.post()
                .uri("/deal/offer/select")
                .body(request)
                .retrieve()
                .toEntity(Void.class);
        }
        catch (Exception ex) {
            log.error("Error selecting offer", ex);
            throw ex;
        }
    }
}
