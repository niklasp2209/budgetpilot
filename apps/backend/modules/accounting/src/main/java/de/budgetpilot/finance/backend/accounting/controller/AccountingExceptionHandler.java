package de.budgetpilot.finance.backend.accounting.controller;

import de.budgetpilot.finance.backend.accounting.exception.AccountingAccessDeniedException;
import de.budgetpilot.finance.backend.accounting.exception.AccountingConflictException;
import de.budgetpilot.finance.backend.accounting.exception.AccountingNotFoundException;
import de.budgetpilot.finance.backend.auth.controller.ErrorResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author Niklas Petermeier
 * @since 06.05.2026
 */
@RestControllerAdvice
public class AccountingExceptionHandler {
    @ExceptionHandler(AccountingAccessDeniedException.class)
    ResponseEntity<ErrorResponse> handleForbidden(@NonNull AccountingAccessDeniedException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse("ACCOUNTING_FORBIDDEN", exception.getMessage()));
    }

    @ExceptionHandler(AccountingNotFoundException.class)
    ResponseEntity<ErrorResponse> handleNotFound(@NonNull AccountingNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("ACCOUNTING_NOT_FOUND", exception.getMessage()));
    }

    @ExceptionHandler(AccountingConflictException.class)
    ResponseEntity<ErrorResponse> handleConflict(@NonNull AccountingConflictException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("ACCOUNTING_CONFLICT", exception.getMessage()));
    }
}

