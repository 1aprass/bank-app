package ru.neoflex.deal.controller;


import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.neoflex.deal.dto.FinishRegistrationRequestDto;
import ru.neoflex.deal.dto.LoanOfferDto;
import ru.neoflex.deal.dto.LoanStatementRequestDto;
import ru.neoflex.deal.service.DealService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/deal")
@RequiredArgsConstructor
public class DealController {

    private final DealService dealService;
    @PostMapping("/statement")
    @Operation(summary = "Create a loan statement and get offers")
    public ResponseEntity<List<LoanOfferDto>> createStatement(
            @Valid @RequestBody LoanStatementRequestDto request){

        log.info("Input. DealController createStatement(): request sent");
        List<LoanOfferDto> response = dealService.createStatement(request);
        log.info("Output. DealController createStatement(): received response and size offer={}", response.size());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/offer/select")
    @Operation(summary = "Select one offer from four previous")
    public ResponseEntity<Void> selectOffer(@Valid @RequestBody LoanOfferDto request){
        log.info("Input. DealController selectOffer(): request sent");
        dealService.selectOffer(request);
        log.info("Output. DealController selectOffer(): request received");
        return ResponseEntity.ok().build();
    }

    @PostMapping("/calculate/{statementId}")
    @Operation(summary = "Finishing registration and calculating final loan payment")
    public ResponseEntity<Void> finishRegistration(@Valid @RequestBody FinishRegistrationRequestDto finishRegistrationRequestDto,
                                                   @PathVariable String statementId ){
        log.info("Input. DealController finishRegistration(): request sent");
        dealService.finishRegistration(finishRegistrationRequestDto, statementId);
        log.info("Output. DealController finishRegistration(): request received");
        return ResponseEntity.ok().build();
    }

}
