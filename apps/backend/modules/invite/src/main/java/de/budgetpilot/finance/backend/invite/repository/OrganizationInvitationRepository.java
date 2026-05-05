package de.budgetpilot.finance.backend.invite.repository;

import de.budgetpilot.finance.backend.invite.domain.OrganizationInvitationEntity;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * @author Niklas Petermeier
 * @since 05.05.2026
 */
public interface OrganizationInvitationRepository extends JpaRepository<OrganizationInvitationEntity, UUID> {
    /**
     * Finds an invitation by token.
     *
     * @param token invitation token
     * @return invitation if present
     */
    @NonNull Optional<OrganizationInvitationEntity> findByToken(@NonNull String token);

    /**
     * Returns all invitations for one organization.
     *
     * @param organizationId organization identifier
     * @return invitations belonging to the organization
     */
    @NonNull List<OrganizationInvitationEntity> findByOrganizationId(@NonNull UUID organizationId);
}
