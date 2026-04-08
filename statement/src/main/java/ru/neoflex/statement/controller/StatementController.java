package ru.neoflex.statement.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import ru.neoflex.statement.service.StatementService;
import ru.neoflex.statement.dto.LoanOfferDto;
import ru.neoflex.statement.dto.LoanStatementRequestDto;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/statement")
public class StatementController {

    private final StatementService statementService;

    @PostMapping()
    @Operation(summary = "Create 4 offers")
    public ResponseEntity<List<LoanOfferDto>> prescoring(@Valid @RequestBody LoanStatementRequestDto request){
        log.info("Input. StatementController prescoring(): request sent");
        List<LoanOfferDto> response = statementService.prescoring(request);
        log.info("Output. StatementController prescoring(): received response and size offer={}", response.size());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/offer")
    @Operation(summary = "Select one offer from four previous")
    public ResponseEntity<Void> selectOffer(@Valid @RequestBody LoanOfferDto request){
        log.info("Input. StatementController selectOffer(): request sent");
        statementService.selectOffer(request);
        log.info("Output. StatementController selectOffer()");
        return ResponseEntity.ok().build();

    }
}
