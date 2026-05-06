package de.budgetpilot.finance.backend.me.dto;

import de.budgetpilot.finance.backend.organization.domain.MembershipRole;
import org.jspecify.annotations.NonNull;

import java.util.UUID;

/**
 * @author Niklas Petermeier
 * @since 06.05.2026
 */
public record MyOrganizationResponse(
        @NonNull UUID id,
        @NonNull String name,
        @NonNull String slug,
        @NonNull MembershipRole role
) {
}

