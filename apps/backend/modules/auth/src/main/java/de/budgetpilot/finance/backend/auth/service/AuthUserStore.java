package de.budgetpilot.finance.backend.auth.service;

import de.budgetpilot.finance.backend.auth.domain.AuthUser;
import de.budgetpilot.finance.backend.auth.domain.AuthUserEntity;
import de.budgetpilot.finance.backend.auth.repository.AuthUserRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * @author Niklas Petermeier
 * @since 05.05.2026
 */
@Component
@RequiredArgsConstructor
public class AuthUserStore {
    private final AuthUserRepository authUserRepository;
    private final JdbcTemplate jdbcTemplate;

    /**
     * Finds a user by email.
     *
     * @param email user email address
     * @return optional user if present
     */
    public @NonNull Optional<AuthUser> findByEmail(@NonNull String email) {
        return authUserRepository.findByEmail(normalize(email))
                .map(entity -> new AuthUser(entity.getId(), entity.getEmail(), entity.getPasswordHash()));
    }

    /**
     * Creates a user if the email is not already present.
     *
     * @param email user email to store
     * @param passwordHash hashed password
     * @return optional created user if created, otherwise empty
     */
    @Transactional
    public @NonNull Optional<AuthUser> createUser(@NonNull String email, @NonNull String passwordHash) {
        String normalizedEmail = normalize(email);
        boolean exists = authUserRepository.findByEmail(normalizedEmail).isPresent();
        if (exists) {
            return Optional.empty();
        }
        AuthUserEntity entity = AuthUserEntity.createNew(normalizedEmail, passwordHash);
        AuthUserEntity saved = authUserRepository.save(entity);
        return Optional.of(new AuthUser(saved.getId(), saved.getEmail(), saved.getPasswordHash()));
    }

    /**
     * Clears all users from the database.
     */
    @Transactional
    public void clear() {
        jdbcTemplate.execute("DELETE FROM budget_items");
        jdbcTemplate.execute("DELETE FROM budgets");
        jdbcTemplate.execute("DELETE FROM transactions");
        jdbcTemplate.execute("DELETE FROM categories");
        jdbcTemplate.execute("DELETE FROM accounts");
        jdbcTemplate.execute("DELETE FROM organization_member_permission_groups");
        jdbcTemplate.execute("DELETE FROM organization_permission_group_permissions");
        jdbcTemplate.execute("DELETE FROM organization_permission_groups");
        jdbcTemplate.execute("DELETE FROM organization_invitations");
        jdbcTemplate.execute("DELETE FROM organization_memberships");
        jdbcTemplate.execute("DELETE FROM organizations");
        jdbcTemplate.execute("DELETE FROM refresh_tokens");
        jdbcTemplate.execute("DELETE FROM users");
    }

    private String normalize(@NonNull String email) {
        return email.trim().toLowerCase();
    }
}
