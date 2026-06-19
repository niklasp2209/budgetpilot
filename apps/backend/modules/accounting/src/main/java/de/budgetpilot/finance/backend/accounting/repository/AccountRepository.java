package de.budgetpilot.finance.backend.accounting.repository;

import de.budgetpilot.finance.backend.accounting.domain.AccountEntity;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

/**
 * @author Niklas Petermeier
 * @since 06.05.2026
 */
public interface AccountRepository extends JpaRepository<AccountEntity, UUID> {
    @NonNull List<AccountEntity> findByOrganizationId(@NonNull UUID organizationId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE AccountEntity a SET a.currency = :currency WHERE a.organizationId = :organizationId")
    void updateCurrencyByOrganizationId(
            @Param("organizationId") @NonNull UUID organizationId,
            @Param("currency") @NonNull String currency
    );
}

