package de.budgetpilot.finance.backend.organization.controller;

import de.budgetpilot.finance.backend.auth.controller.ErrorResponse;
import de.budgetpilot.finance.backend.organization.exception.OrganizationAccessDeniedException;
import de.budgetpilot.finance.backend.organization.exception.OrganizationNotFoundException;
import de.budgetpilot.finance.backend.organization.exception.OrganizationSlugAlreadyExistsException;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author Niklas Petermeier
 * @since 05.05.2026
 */
@RestControllerAdvice
public class OrganizationExceptionHandler {
    @ExceptionHandler(OrganizationSlugAlreadyExistsException.class)
    ResponseEntity<ErrorResponse> handleSlugExists(@NonNull OrganizationSlugAlreadyExistsException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("ORGANIZATION_SLUG_EXISTS", exception.getMessage()));
    }

    @ExceptionHandler(OrganizationNotFoundException.class)
    ResponseEntity<ErrorResponse> handleNotFound(@NonNull OrganizationNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("ORGANIZATION_NOT_FOUND", exception.getMessage()));
    }

    @ExceptionHandler(OrganizationAccessDeniedException.class)
    ResponseEntity<ErrorResponse> handleForbidden(@NonNull OrganizationAccessDeniedException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse("ORGANIZATION_FORBIDDEN", exception.getMessage()));
    }
}
