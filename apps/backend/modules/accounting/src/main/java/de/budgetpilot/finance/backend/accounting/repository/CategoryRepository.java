package de.budgetpilot.finance.backend.accounting.repository;

import de.budgetpilot.finance.backend.accounting.domain.CategoryEntity;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * @author Niklas Petermeier
 * @since 06.05.2026
 */
public interface CategoryRepository extends JpaRepository<CategoryEntity, UUID> {
    @NonNull List<CategoryEntity> findByOrganizationId(@NonNull UUID organizationId);
}

