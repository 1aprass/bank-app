package ru.neoflex.calculator.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OfferResponse {
    private List<LoanOfferDto> offers;
    private String rejectionReason;
    private boolean success;

}
