package ru.neoflex.deal.exception;

public class ScoringDeniedException extends RuntimeException {

    public ScoringDeniedException(String message) {
        super(message);
    }

    public ScoringDeniedException(String message, Throwable cause) {
        super(message, cause);
    }
}
