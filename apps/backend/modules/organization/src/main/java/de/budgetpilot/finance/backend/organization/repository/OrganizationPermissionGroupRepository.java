package de.budgetpilot.finance.backend.organization.repository;

import de.budgetpilot.finance.backend.organization.domain.OrganizationPermissionGroupEntity;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * @author Niklas Petermeier
 * @since 18.06.2026
 */
public interface OrganizationPermissionGroupRepository extends JpaRepository<OrganizationPermissionGroupEntity, UUID> {
    @NonNull List<OrganizationPermissionGroupEntity> findByOrganizationId(@NonNull UUID organizationId);

    @NonNull Optional<OrganizationPermissionGroupEntity> findByOrganizationIdAndName(
            @NonNull UUID organizationId,
            @NonNull String name
    );
}
