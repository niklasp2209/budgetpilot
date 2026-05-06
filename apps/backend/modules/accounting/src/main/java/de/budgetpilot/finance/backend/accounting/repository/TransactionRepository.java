package de.budgetpilot.finance.backend.accounting.repository;

import de.budgetpilot.finance.backend.accounting.domain.TransactionEntity;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

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
}

