package de.budgetpilot.finance.backend.organization.mapper;

import de.budgetpilot.finance.backend.organization.domain.OrganizationEntity;
import de.budgetpilot.finance.backend.organization.domain.OrganizationMembershipEntity;
import de.budgetpilot.finance.backend.organization.dto.OrganizationMemberResponse;
import de.budgetpilot.finance.backend.organization.dto.OrganizationResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

/**
 * @author Niklas Petermeier
 * @since 05.05.2026
 */
@Component
public class OrganizationMapper {
    public @NonNull OrganizationResponse toOrganizationResponse(@NonNull OrganizationEntity entity) {
        return new OrganizationResponse(entity.getId(), entity.getName(), entity.getSlug());
    }

    public @NonNull OrganizationMemberResponse toOrganizationMemberResponse(@NonNull OrganizationMembershipEntity entity) {
        return new OrganizationMemberResponse(
                entity.getId().getUserId(),
                entity.getRole(),
                entity.getStatus()
        );
    }
}
