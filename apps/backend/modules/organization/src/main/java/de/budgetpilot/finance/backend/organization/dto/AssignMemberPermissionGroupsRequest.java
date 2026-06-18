package de.budgetpilot.finance.backend.organization.dto;

import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.Set;
import java.util.UUID;

/**
 * @author Niklas Petermeier
 * @since 18.06.2026
 */
public record AssignMemberPermissionGroupsRequest(
        @NonNull @NotNull Set<UUID> groupIds
) {
}
