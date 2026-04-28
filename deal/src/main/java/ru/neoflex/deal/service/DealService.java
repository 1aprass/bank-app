package ru.neoflex.deal.service;

import org.springframework.web.bind.annotation.PathVariable;
import ru.neoflex.deal.dto.FinishRegistrationRequestDto;
import ru.neoflex.deal.dto.LoanOfferDto;
import ru.neoflex.deal.dto.LoanStatementRequestDto;

import java.util.List;

public interface DealService {
    List<LoanOfferDto> createStatement(LoanStatementRequestDto request);
    void selectOffer(LoanOfferDto request);
    void finishRegistration(FinishRegistrationRequestDto requestDto, String statementId);
    void sendDocuments(String statementId);
    void requestSignDocuments(String statementId);
    void signDocuments(String statementId, String sesCode);
}
