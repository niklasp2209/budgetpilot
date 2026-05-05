package de.budgetpilot.finance.backend.auth.repository;

import de.budgetpilot.finance.backend.auth.domain.AuthUserEntity;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * @author Niklas Petermeier
 * @since 05.05.2026
 */
public interface AuthUserRepository extends JpaRepository<AuthUserEntity, UUID> {
    @NonNull Optional<AuthUserEntity> findByEmail(@NonNull String email);
}
