package ru.neoflex.gateway.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import ru.neoflex.gateway.dto.FinishRegistrationRequestDto;
import ru.neoflex.gateway.dto.LoanOfferDto;
import ru.neoflex.gateway.dto.LoanStatementRequestDto;
import ru.neoflex.gateway.dto.StatementDto;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api")
public class GatewayController {

    private final RestClient dealRestClient;
    private final RestClient statementRestClient;

    public GatewayController(
            @Qualifier("dealRestClient") RestClient dealRestClient,
            @Qualifier("statementRestClient") RestClient statementRestClient) {
        this.dealRestClient = dealRestClient;
        this.statementRestClient = statementRestClient;
    }

    @PostMapping("/statement")
    @Operation(summary = "Create a loan statement and get offers")
    public ResponseEntity<List<LoanOfferDto>> createStatement(
            @Valid @RequestBody LoanStatementRequestDto request){

        log.info("Input. DealGatewayController createStatement(). request sent");
        List<LoanOfferDto> response = dealRestClient.post()
                .uri("/deal/statement")
                .body(request)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});

        log.info("Output. DealGatewayController createStatement()");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/offer/select")
    @Operation(summary = "Select one offer from four previous")
    public ResponseEntity<Void> selectOffer(@Valid @RequestBody LoanOfferDto request){
        log.info("Input. DealGatewayController selectOffer(): request sent");
        dealRestClient.post()
                .uri("/deal/offer/select")
                .body(request)
                .retrieve()
                .toBodilessEntity();

        log.info("Output. DealGatewayController selectOffer(): request received");
        return ResponseEntity.ok().build();
    }

    @PostMapping("/calculate/{statementId}")
    @Operation(summary = "Finishing registration and calculating final loan payment")
    public ResponseEntity<Void> finishRegistration(@Valid @RequestBody FinishRegistrationRequestDto finishRegistrationRequestDto,
                                                   @PathVariable String statementId ){
        log.info("Input. DealGatewayController finishRegistration(): request sent");
        dealRestClient.post()
                .uri("/deal/calculate/{statementId}", statementId)
                .body(finishRegistrationRequestDto)
                .retrieve()
                .toBodilessEntity();

        log.info("Output. DealGatewayController finishRegistration(): request received");
        return ResponseEntity.ok().build();
    }

    @PostMapping("/document/{statementId}/send")
    @Operation(summary = "request to send documents to the client")
    public ResponseEntity<Void> sendDocuments(@PathVariable String statementId) {
        log.info("Input. DealGatewayController sendDocuments(). Request with {} sent", statementId);

        dealRestClient.post()
                .uri("/deal/document/{statementId}/send", statementId)
                .retrieve()
                .toBodilessEntity();

        return ResponseEntity.ok().build();
    }

    @PostMapping("/document/{statementId}/sign")
    @Operation(summary = "request to sign documents to the client")
    public ResponseEntity<Void> requestSignDocuments(@PathVariable String statementId) {
        log.info("Input. DealGatewayController requestSignDocuments(). Request with {} sent", statementId);

        dealRestClient.post()
                .uri("/deal/document/{statementId}/sign", statementId)
                .retrieve()
                .toBodilessEntity();

        return ResponseEntity.ok().build();
    }

    @PostMapping("/document/{statementId}/code")
    @Operation(summary = "sign documents")
    public ResponseEntity<Void> signDocuments(
            @PathVariable String statementId,
            @RequestParam String code) {
        log.info("Input. DealGatewayController signDocuments(). Request with {} sent", statementId);

        dealRestClient.post()
                .uri("/deal/document/{statementId}/code?code={code}", statementId, code)
                .retrieve()
                .toBodilessEntity();

        return ResponseEntity.ok().build();
    }

    @GetMapping("/admin/statement/{statementId}")
    @Operation(summary = "get statement by id")
    public ResponseEntity<StatementDto> getStatementById(@PathVariable String statementId){
        log.info("Input. DealGatewayController getStatement(). Request with {} sent", statementId);

        StatementDto response = dealRestClient.get()
                .uri("/deal/admin/statement/{statementId}", statementId)
                .retrieve()
                .body(StatementDto.class);
        log.info("Output. DealGatewayController getStatement(). Response was received");
        return ResponseEntity.ok(response);

    }

    @GetMapping("/admin/statement")
    @Operation(summary = "get all statements")
    public ResponseEntity<List<StatementDto>> getAllStatements(){
        log.info("Input. DealGatewayController getStatement(). Request sent");
        List<StatementDto> response = dealRestClient.get()
                .uri("/deal/admin/statement")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});

        List<StatementDto> result = response != null ? response : List.of();
        log.info("Output. DealGatewayController getAllStatements(). Received {} statements", result.size());
        return ResponseEntity.ok(result);
    }

    @PostMapping()
    @Operation(summary = "Create 4 offers")
    public ResponseEntity<List<LoanOfferDto>> prescoring(@Valid @RequestBody LoanStatementRequestDto request){
        log.info("Input. DealGatewayController prescoring(): request sent");
        List<LoanOfferDto> response = statementRestClient.post()
                .uri("/statement")
                .body(request)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});

        log.info("Output. DealGatewayController prescoring()");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/offer")
    @Operation(summary = "Select one offer from four previous")
    public ResponseEntity<Void> selectFourOffer(@Valid @RequestBody LoanOfferDto request){
        log.info("Input. DealGatewayController selectFourOffer(): request sent");
        statementRestClient.post()
                .uri("/statement/offer")
                .body(request)
                .retrieve()
                .toBodilessEntity();

        log.info("Output. DealGatewayController selectFourOffer()");
        return ResponseEntity.ok().build();

    }
}
