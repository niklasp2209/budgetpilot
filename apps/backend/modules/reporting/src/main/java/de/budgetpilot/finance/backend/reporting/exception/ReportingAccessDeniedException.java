package de.budgetpilot.finance.backend.reporting.exception;

import org.jspecify.annotations.NonNull;

/**
 * @author Niklas Petermeier
 * @since 11.05.2026
 */
public class ReportingAccessDeniedException extends RuntimeException {
    public ReportingAccessDeniedException(@NonNull String message) {
        super(message);
    }
}
