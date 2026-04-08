package ru.neoflex.statement.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.neoflex.statement.service.StatementService;
import ru.neoflex.statement.client.StatementClient;
import ru.neoflex.statement.dto.LoanOfferDto;
import ru.neoflex.statement.dto.LoanStatementRequestDto;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatementServiceImpl implements StatementService {
    private final StatementClient client;
    @Override
    public List<LoanOfferDto> prescoring(LoanStatementRequestDto request) {
        log.info("StatementService. Input prescoring(), LoanStatementRequestDto request={}", request);
        List <LoanOfferDto> response = client.prescoring(request);
        log.debug("StatementService. prescoring(). LoanOfferDto list: {}", response);
        log.info("StatementService. Output. prescoring(), offers count={}", response.size());
        return response;
    }

    @Override
    public void selectOffer(LoanOfferDto request) {
        log.info("StatementService. Input selectOffer(), LoanOfferDto request={}", request);
        client.selectOffer(request);
        log.info("StatementService. Output. prescoring(). End of client.selectOffer(request)");

    }

}
