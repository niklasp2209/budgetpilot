package de.budgetpilot.finance.backend.invite.controller;

import de.budgetpilot.finance.backend.auth.controller.ErrorResponse;
import de.budgetpilot.finance.backend.invite.exception.InviteAccessDeniedException;
import de.budgetpilot.finance.backend.invite.exception.InviteInvalidException;
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
public class InviteExceptionHandler {
    /**
     * Maps invite access exceptions to HTTP 403 responses.
     *
     * @param exception thrown invite access exception
     * @return forbidden API error payload
     */
    @ExceptionHandler(InviteAccessDeniedException.class)
    ResponseEntity<ErrorResponse> handleForbidden(@NonNull InviteAccessDeniedException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse("INVITE_FORBIDDEN", exception.getMessage()));
    }

    /**
     * Maps invalid invite exceptions to HTTP 400 responses.
     *
     * @param exception thrown invalid invite exception
     * @return bad request API error payload
     */
    @ExceptionHandler(InviteInvalidException.class)
    ResponseEntity<ErrorResponse> handleInvalid(@NonNull InviteInvalidException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("INVITE_INVALID", exception.getMessage()));
    }
}
