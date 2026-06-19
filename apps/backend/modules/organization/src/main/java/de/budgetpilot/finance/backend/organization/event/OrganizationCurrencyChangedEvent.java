package de.budgetpilot.finance.backend.organization.event;

import org.jspecify.annotations.NonNull;

import java.util.UUID;

/**
 * @author Niklas Petermeier
 * @since 19.06.2026
 */
public record OrganizationCurrencyChangedEvent(
        @NonNull UUID organizationId,
        @NonNull String currency
) {
}
