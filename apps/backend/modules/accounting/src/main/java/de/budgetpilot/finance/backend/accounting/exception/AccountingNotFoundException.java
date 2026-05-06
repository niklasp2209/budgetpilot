package de.budgetpilot.finance.backend.accounting.exception;

import org.jspecify.annotations.NonNull;

/**
 * @author Niklas Petermeier
 * @since 06.05.2026
 */
public class AccountingNotFoundException extends RuntimeException {
    public AccountingNotFoundException(@NonNull String message) {
        super(message);
    }
}

