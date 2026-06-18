package de.budgetpilot.finance.backend.organization.repository;

import de.budgetpilot.finance.backend.organization.domain.OrganizationMemberPermissionGroupEntity;
import de.budgetpilot.finance.backend.organization.domain.OrganizationMemberPermissionGroupId;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * @author Niklas Petermeier
 * @since 18.06.2026
 */
public interface OrganizationMemberPermissionGroupRepository
        extends JpaRepository<OrganizationMemberPermissionGroupEntity, OrganizationMemberPermissionGroupId> {
    @NonNull List<OrganizationMemberPermissionGroupEntity> findByIdOrganizationIdAndIdUserId(
            @NonNull UUID organizationId,
            @NonNull UUID userId
    );

    void deleteByIdOrganizationIdAndIdUserId(@NonNull UUID organizationId, @NonNull UUID userId);
}
