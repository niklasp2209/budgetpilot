package de.budgetpilot.finance.backend.budget.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jspecify.annotations.NonNull;

import java.time.LocalDate;
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
@Table(name = "budgets")
public class BudgetEntity {
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "organization_id", nullable = false, updatable = false)
    private UUID organizationId;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "period_start", nullable = false, updatable = false)
    private LocalDate periodStart;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public static @NonNull BudgetEntity createNew(
            @NonNull UUID organizationId,
            @NonNull String name,
            @NonNull LocalDate periodStart,
            @NonNull String currency
    ) {
        BudgetEntity entity = new BudgetEntity();
        entity.setId(UUID.randomUUID());
        entity.setOrganizationId(organizationId);
        entity.setName(name);
        entity.setPeriodStart(periodStart);
        entity.setCurrency(currency);
        entity.setCreatedAt(OffsetDateTime.now());
        return entity;
    }
}

