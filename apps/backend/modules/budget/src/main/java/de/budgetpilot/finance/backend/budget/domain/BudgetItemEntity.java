package de.budgetpilot.finance.backend.budget.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jspecify.annotations.NonNull;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * @author Niklas Petermeier
 * @since 11.05.2026
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "budget_items")
public class BudgetItemEntity {
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "budget_id", nullable = false, updatable = false)
    private UUID budgetId;

    @Column(name = "category_id", nullable = false, updatable = false)
    private UUID categoryId;

    @Column(name = "amount_cents", nullable = false)
    private long amountCents;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public static @NonNull BudgetItemEntity createNew(
            @NonNull UUID budgetId,
            @NonNull UUID categoryId,
            long amountCents
    ) {
        BudgetItemEntity entity = new BudgetItemEntity();
        entity.setId(UUID.randomUUID());
        entity.setBudgetId(budgetId);
        entity.setCategoryId(categoryId);
        entity.setAmountCents(amountCents);
        entity.setCreatedAt(OffsetDateTime.now());
        return entity;
    }
}

