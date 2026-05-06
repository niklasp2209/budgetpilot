package de.budgetpilot.finance.backend.organization.repository;

import de.budgetpilot.finance.backend.organization.domain.OrganizationMembershipEntity;
import de.budgetpilot.finance.backend.organization.domain.OrganizationMembershipId;
import de.budgetpilot.finance.backend.organization.domain.MembershipRole;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * @author Niklas Petermeier
 * @since 05.05.2026
 */
public interface OrganizationMembershipRepository extends JpaRepository<OrganizationMembershipEntity, OrganizationMembershipId> {
    @NonNull List<OrganizationMembershipEntity> findByIdOrganizationId(@NonNull UUID organizationId);

    @NonNull Optional<OrganizationMembershipEntity> findByIdOrganizationIdAndIdUserId(@NonNull UUID organizationId, @NonNull UUID userId);

    @NonNull List<OrganizationMembershipEntity> findByIdUserId(@NonNull UUID userId);

    long countByIdOrganizationIdAndRole(@NonNull UUID organizationId, @NonNull MembershipRole role);
}
