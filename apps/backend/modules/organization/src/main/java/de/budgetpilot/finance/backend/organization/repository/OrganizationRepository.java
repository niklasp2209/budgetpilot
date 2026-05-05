package de.budgetpilot.finance.backend.organization.repository;

import de.budgetpilot.finance.backend.organization.domain.OrganizationEntity;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * @author Niklas Petermeier
 * @since 05.05.2026
 */
public interface OrganizationRepository extends JpaRepository<OrganizationEntity, UUID> {
    @NonNull Optional<OrganizationEntity> findBySlug(@NonNull String slug);
}
