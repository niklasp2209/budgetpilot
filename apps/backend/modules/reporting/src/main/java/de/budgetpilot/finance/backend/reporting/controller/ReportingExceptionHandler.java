package de.budgetpilot.finance.backend.reporting.controller;

import de.budgetpilot.finance.backend.auth.controller.ErrorResponse;
import de.budgetpilot.finance.backend.reporting.exception.ReportingAccessDeniedException;
import de.budgetpilot.finance.backend.reporting.exception.ReportingNotFoundException;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author Niklas Petermeier
 * @since 11.05.2026
 */
@RestControllerAdvice
public class ReportingExceptionHandler {
    @ExceptionHandler(ReportingAccessDeniedException.class)
    ResponseEntity<ErrorResponse> handleForbidden(@NonNull ReportingAccessDeniedException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse("REPORTING_FORBIDDEN", exception.getMessage()));
    }

    @ExceptionHandler(ReportingNotFoundException.class)
    ResponseEntity<ErrorResponse> handleNotFound(@NonNull ReportingNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("REPORTING_NOT_FOUND", exception.getMessage()));
    }
}
