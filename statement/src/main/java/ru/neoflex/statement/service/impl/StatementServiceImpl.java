package ru.neoflex.statement.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.neoflex.statement.service.StatementService;
import ru.neoflex.statement.client.StatementClient;
import ru.neoflex.statement.dto.LoanOfferDto;
import ru.neoflex.statement.dto.LoanStatementRequestDto;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatementServiceImpl implements StatementService {
    private final StatementClient client;
    @Override
    public List<LoanOfferDto> prescoring(LoanStatementRequestDto request) {
        log.info("StatementService. Input prescoring(), LoanStatementRequestDto request={}", request);
        makePrescoring(request);
        List <LoanOfferDto> response = client.prescoring(request);
        log.debug("StatementService. prescoring(). LoanOfferDto list: {}", response);
        log.info("StatementService. Output. prescoring(), offers count={}", response.size());
        return response;
    }

    public void makePrescoring(LoanStatementRequestDto request){
        log.debug("Input. StatementServiceImpl makePrescoring() - client age verification");
        if (request.getBirthdate() != null) {
            int age = Period.between(request.getBirthdate(), LocalDate.now()).getYears();
            if (age < 18) {
                log.error("Error. StatementServiceImpl makePrescoring() - Client is younger then 18: age {}", age);
                throw new IllegalArgumentException("Client must be at least 18 years old");
            }
            log.debug("Output. StatementServiceImpl makePrescoring() - Client`s age {}", age);
        } else {
            log.error("Error. StatementServiceImpl makePrescoring() - There isn`t date of birth");
            throw new IllegalArgumentException("Birthdate is required");
        }
    }

    @Override
    public void selectOffer(LoanOfferDto request) {
        log.info("StatementService. Input selectOffer(), LoanOfferDto request={}", request);
        client.selectOffer(request);
        log.info("StatementService. Output. prescoring(). End of client.selectOffer(request)");

    }

}
