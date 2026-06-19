package de.budgetpilot.finance.backend.organization.mapper;

import de.budgetpilot.finance.backend.organization.domain.OrganizationEntity;
import de.budgetpilot.finance.backend.organization.domain.OrganizationMembershipEntity;
import de.budgetpilot.finance.backend.organization.dto.OrganizationMemberResponse;
import de.budgetpilot.finance.backend.organization.dto.OrganizationResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

/**
 * @author Niklas Petermeier
 * @since 05.05.2026
 */
@Component
public class OrganizationMapper {
    /**
     * Maps organization entity to API response.
     *
     * @param entity organization entity
     * @return organization response
     */
    public @NonNull OrganizationResponse toOrganizationResponse(@NonNull OrganizationEntity entity) {
        return new OrganizationResponse(entity.getId(), entity.getName(), entity.getSlug(), entity.getCurrency());
    }

    /**
     * Maps membership entity to API response.
     *
     * @param entity membership entity
     * @return organization member response
     */
    public @NonNull OrganizationMemberResponse toOrganizationMemberResponse(
            @NonNull OrganizationMembershipEntity entity,
            @NonNull String email,
            @NonNull Set<UUID> permissionGroupIds
    ) {
        return new OrganizationMemberResponse(
                entity.getId().getUserId(),
                email,
                entity.getRole(),
                entity.getStatus(),
                Set.copyOf(permissionGroupIds)
        );
    }
}
