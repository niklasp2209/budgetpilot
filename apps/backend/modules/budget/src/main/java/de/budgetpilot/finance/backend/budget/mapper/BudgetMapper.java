package de.budgetpilot.finance.backend.budget.mapper;

import de.budgetpilot.finance.backend.budget.domain.BudgetEntity;
import de.budgetpilot.finance.backend.budget.domain.BudgetItemEntity;
import de.budgetpilot.finance.backend.budget.dto.BudgetItemDetailResponse;
import de.budgetpilot.finance.backend.budget.dto.BudgetItemResponse;
import de.budgetpilot.finance.backend.budget.dto.BudgetResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

/**
 * @author Niklas Petermeier
 * @since 11.05.2026
 */
@Component
public class BudgetMapper {
    public @NonNull BudgetResponse toBudgetResponse(@NonNull BudgetEntity entity) {
        return new BudgetResponse(entity.getId(), entity.getName(), entity.getPeriodStart(), entity.getCurrency());
    }

    public @NonNull BudgetItemResponse toBudgetItemResponse(@NonNull BudgetItemEntity entity) {
        return new BudgetItemResponse(entity.getId(), entity.getCategoryId(), entity.getAmountCents());
    }

    public @NonNull BudgetItemDetailResponse toBudgetItemDetailResponse(
            @NonNull BudgetItemEntity entity,
            @NonNull String categoryName
    ) {
        return new BudgetItemDetailResponse(
                entity.getId(),
                entity.getCategoryId(),
                categoryName,
                entity.getAmountCents()
        );
    }
}

