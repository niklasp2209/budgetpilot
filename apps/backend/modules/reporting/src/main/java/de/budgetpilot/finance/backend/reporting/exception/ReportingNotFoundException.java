package de.budgetpilot.finance.backend.reporting.exception;

import org.jspecify.annotations.NonNull;

/**
 * @author Niklas Petermeier
 * @since 11.05.2026
 */
public class ReportingNotFoundException extends RuntimeException {
    public ReportingNotFoundException(@NonNull String message) {
        super(message);
    }
}
