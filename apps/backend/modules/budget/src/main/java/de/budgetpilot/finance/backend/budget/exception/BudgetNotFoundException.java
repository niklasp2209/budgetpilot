package de.budgetpilot.finance.backend.budget.exception;

import org.jspecify.annotations.NonNull;

/**
 * @author Niklas Petermeier
 * @since 11.05.2026
 */
public class BudgetNotFoundException extends RuntimeException {
    public BudgetNotFoundException(@NonNull String message) {
        super(message);
    }
}

