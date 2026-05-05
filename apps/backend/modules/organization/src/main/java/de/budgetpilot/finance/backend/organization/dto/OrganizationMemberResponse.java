package de.budgetpilot.finance.backend.organization.dto;

import de.budgetpilot.finance.backend.organization.domain.MembershipRole;
import org.jspecify.annotations.NonNull;

import java.util.UUID;

/**
 * @author Niklas Petermeier
 * @since 05.05.2026
 */
public record OrganizationMemberResponse(
        @NonNull UUID userId,
        @NonNull MembershipRole role,
        @NonNull String status
) {
}
