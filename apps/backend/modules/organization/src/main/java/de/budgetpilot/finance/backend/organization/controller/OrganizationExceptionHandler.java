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
    /**
     * Maps duplicate slug errors to HTTP 409.
     *
     * @param exception duplicate slug exception
     * @return conflict response payload
     */
    @ExceptionHandler(OrganizationSlugAlreadyExistsException.class)
    ResponseEntity<ErrorResponse> handleSlugExists(@NonNull OrganizationSlugAlreadyExistsException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("ORGANIZATION_SLUG_EXISTS", exception.getMessage()));
    }

    /**
     * Maps organization not found errors to HTTP 404.
     *
     * @param exception not found exception
     * @return not found response payload
     */
    @ExceptionHandler(OrganizationNotFoundException.class)
    ResponseEntity<ErrorResponse> handleNotFound(@NonNull OrganizationNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("ORGANIZATION_NOT_FOUND", exception.getMessage()));
    }

    /**
     * Maps organization access errors to HTTP 403.
     *
     * @param exception access denied exception
     * @return forbidden response payload
     */
    @ExceptionHandler(OrganizationAccessDeniedException.class)
    ResponseEntity<ErrorResponse> handleForbidden(@NonNull OrganizationAccessDeniedException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse("ORGANIZATION_FORBIDDEN", exception.getMessage()));
    }
}
