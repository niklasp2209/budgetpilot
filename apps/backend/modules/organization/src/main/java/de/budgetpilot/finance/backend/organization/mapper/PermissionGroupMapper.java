package de.budgetpilot.finance.backend.organization.mapper;

import de.budgetpilot.finance.backend.organization.domain.OrganizationPermissionGroupEntity;
import de.budgetpilot.finance.backend.organization.dto.PermissionGroupResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * @author Niklas Petermeier
 * @since 18.06.2026
 */
@Component
public class PermissionGroupMapper {
    /**
     * Maps permission group entity to API response.
     *
     * @param entity permission group entity
     * @return permission group response
     */
    public @NonNull PermissionGroupResponse toPermissionGroupResponse(@NonNull OrganizationPermissionGroupEntity entity) {
        return new PermissionGroupResponse(entity.getId(), entity.getName(), Set.copyOf(entity.getPermissions()));
    }
}
