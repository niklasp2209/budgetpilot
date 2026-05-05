package de.budgetpilot.finance.backend.auth.repository;

import de.budgetpilot.finance.backend.auth.domain.RefreshTokenEntity;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * @author Niklas Petermeier
 * @since 05.05.2026
 */
public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, UUID> {
    @NonNull Optional<RefreshTokenEntity> findByTokenHash(@NonNull String tokenHash);
}
