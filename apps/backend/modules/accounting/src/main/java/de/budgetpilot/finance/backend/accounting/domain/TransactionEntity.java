package de.budgetpilot.finance.backend.accounting.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * @author Niklas Petermeier
 * @since 06.05.2026
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "transactions")
public class TransactionEntity {
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "organization_id", nullable = false, updatable = false)
    private UUID organizationId;

    @Column(name = "account_id", nullable = false, updatable = false)
    private UUID accountId;

    @Column(name = "category_id", nullable = false, updatable = false)
    private UUID categoryId;

    @Column(name = "amount_cents", nullable = false)
    private long amountCents;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "booked_at", nullable = false)
    private OffsetDateTime bookedAt;

    @Column(name = "description", length = 1024)
    private @Nullable String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public static @NonNull TransactionEntity createNew(
            @NonNull UUID organizationId,
            @NonNull UUID accountId,
            @NonNull UUID categoryId,
            long amountCents,
            @NonNull String currency,
            @NonNull OffsetDateTime bookedAt,
            @Nullable String description
    ) {
        TransactionEntity entity = new TransactionEntity();
        entity.setId(UUID.randomUUID());
        entity.setOrganizationId(organizationId);
        entity.setAccountId(accountId);
        entity.setCategoryId(categoryId);
        entity.setAmountCents(amountCents);
        entity.setCurrency(currency);
        entity.setBookedAt(bookedAt);
        entity.setDescription(description);
        entity.setCreatedAt(OffsetDateTime.now());
        return entity;
    }
}

