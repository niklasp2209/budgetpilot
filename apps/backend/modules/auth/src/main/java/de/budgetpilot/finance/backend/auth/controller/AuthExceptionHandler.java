package de.budgetpilot.finance.backend.auth.controller;

import de.budgetpilot.finance.backend.auth.exception.EmailAlreadyExistsException;
import de.budgetpilot.finance.backend.auth.exception.InvalidCredentialsException;
import de.budgetpilot.finance.backend.auth.exception.InvalidTokenException;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author Niklas Petermeier
 * @since 05.05.2026
 */
@RestControllerAdvice
public class AuthExceptionHandler {
    @ExceptionHandler(EmailAlreadyExistsException.class)
    ResponseEntity<ErrorResponse> handleEmailExists(@NonNull EmailAlreadyExistsException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("EMAIL_ALREADY_EXISTS", exception.getMessage()));
    }

    @ExceptionHandler({InvalidCredentialsException.class, InvalidTokenException.class})
    ResponseEntity<ErrorResponse> handleUnauthorized(@NonNull RuntimeException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse("AUTH_UNAUTHORIZED", exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponse> handleValidation(@NonNull MethodArgumentNotValidException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("VALIDATION_ERROR", "Invalid request."));
    }

    @ExceptionHandler(AuthenticationException.class)
    ResponseEntity<ErrorResponse> handleAuthentication(@NonNull AuthenticationException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse("AUTH_UNAUTHORIZED", "Authentication required."));
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ErrorResponse> handleForbidden(@NonNull AccessDeniedException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse("AUTH_FORBIDDEN", "Access denied."));
    }
}
