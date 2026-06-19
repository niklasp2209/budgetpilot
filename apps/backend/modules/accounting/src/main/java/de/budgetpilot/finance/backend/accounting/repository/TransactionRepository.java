package de.budgetpilot.finance.backend.accounting.repository;

import de.budgetpilot.finance.backend.accounting.domain.TransactionEntity;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * @author Niklas Petermeier
 * @since 06.05.2026
 */
public interface TransactionRepository extends JpaRepository<TransactionEntity, UUID> {
    @NonNull List<TransactionEntity> findByOrganizationIdAndBookedAtBetween(
            @NonNull UUID organizationId,
            @NonNull OffsetDateTime from,
            @NonNull OffsetDateTime to
    );

    boolean existsByAccountId(@NonNull UUID accountId);

    boolean existsByCategoryId(@NonNull UUID categoryId);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM budget_items WHERE category_id = :categoryId)", nativeQuery = true)
    boolean existsBudgetItemByCategoryId(@Param("categoryId") @NonNull UUID categoryId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE TransactionEntity t SET t.currency = :currency WHERE t.organizationId = :organizationId")
    void updateCurrencyByOrganizationId(
            @Param("organizationId") @NonNull UUID organizationId,
            @Param("currency") @NonNull String currency
    );
}

