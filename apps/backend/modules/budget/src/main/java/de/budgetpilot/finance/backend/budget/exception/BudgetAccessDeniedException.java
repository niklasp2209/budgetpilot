package de.budgetpilot.finance.backend.budget.exception;

import org.jspecify.annotations.NonNull;

/**
 * @author Niklas Petermeier
 * @since 11.05.2026
 */
public class BudgetAccessDeniedException extends RuntimeException {
    public BudgetAccessDeniedException(@NonNull String message) {
        super(message);
    }
}

