package de.budgetpilot.finance.backend.budget.repository;

import de.budgetpilot.finance.backend.budget.domain.BudgetItemEntity;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * @author Niklas Petermeier
 * @since 11.05.2026
 */
public interface BudgetItemRepository extends JpaRepository<BudgetItemEntity, UUID> {
    @NonNull List<BudgetItemEntity> findByBudgetId(@NonNull UUID budgetId);

    @NonNull Optional<BudgetItemEntity> findByBudgetIdAndCategoryId(@NonNull UUID budgetId, @NonNull UUID categoryId);

    @NonNull Optional<BudgetItemEntity> findByIdAndBudgetId(@NonNull UUID id, @NonNull UUID budgetId);
}

