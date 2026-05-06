package de.budgetpilot.finance.backend.organization.dto;

import de.budgetpilot.finance.backend.organization.domain.MembershipRole;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.NonNull;

/**
 * @author Niklas Petermeier
 * @since 06.05.2026
 */
public record UpdateMemberRoleRequest(
        @NonNull @NotNull MembershipRole role
) {
}

