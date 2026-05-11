package de.budgetpilot.finance.backend.budget.repository;

import de.budgetpilot.finance.backend.budget.domain.BudgetEntity;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * @author Niklas Petermeier
 * @since 11.05.2026
 */
public interface BudgetRepository extends JpaRepository<BudgetEntity, UUID> {
    @NonNull List<BudgetEntity> findByOrganizationId(@NonNull UUID organizationId);
}

