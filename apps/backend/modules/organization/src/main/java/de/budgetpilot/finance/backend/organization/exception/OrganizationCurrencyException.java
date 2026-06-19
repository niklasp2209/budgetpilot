package de.budgetpilot.finance.backend.organization.exception;

/**
 * @author Niklas Petermeier
 * @since 19.06.2026
 */
public class OrganizationCurrencyException extends RuntimeException {
    public OrganizationCurrencyException(String message) {
        super(message);
    }
}
