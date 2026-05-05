package de.budgetpilot.finance.backend.organization.dto;

import org.jspecify.annotations.NonNull;

import java.util.UUID;

/**
 * @author Niklas Petermeier
 * @since 05.05.2026
 */
public record OrganizationResponse(
        @NonNull UUID id,
        @NonNull String name,
        @NonNull String slug
) {
}
