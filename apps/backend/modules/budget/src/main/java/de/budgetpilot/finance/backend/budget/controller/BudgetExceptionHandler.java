package de.budgetpilot.finance.backend.budget.controller;

import de.budgetpilot.finance.backend.auth.controller.ErrorResponse;
import de.budgetpilot.finance.backend.budget.exception.BudgetAccessDeniedException;
import de.budgetpilot.finance.backend.budget.exception.BudgetNotFoundException;
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
public class BudgetExceptionHandler {
    @ExceptionHandler(BudgetAccessDeniedException.class)
    ResponseEntity<ErrorResponse> handleForbidden(@NonNull BudgetAccessDeniedException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse("BUDGET_FORBIDDEN", exception.getMessage()));
    }

    @ExceptionHandler(BudgetNotFoundException.class)
    ResponseEntity<ErrorResponse> handleNotFound(@NonNull BudgetNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("BUDGET_NOT_FOUND", exception.getMessage()));
    }
}

