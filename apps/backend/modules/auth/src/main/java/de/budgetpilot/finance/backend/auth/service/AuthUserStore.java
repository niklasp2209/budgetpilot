package de.budgetpilot.finance.backend.auth.service;

import de.budgetpilot.finance.backend.auth.domain.AuthUser;
import de.budgetpilot.finance.backend.auth.domain.AuthUserEntity;
import de.budgetpilot.finance.backend.auth.repository.AuthUserRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
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

    /**
     * Finds a user by email.
     *
     * @param email user email address
     * @return optional user if present
     */
    public @NonNull Optional<AuthUser> findByEmail(@NonNull String email) {
        return authUserRepository.findByEmail(normalize(email))
                .map(entity -> new AuthUser(entity.getEmail(), entity.getPasswordHash()));
    }

    /**
     * Creates a user if the email is not already present.
     *
     * @param user user to store
     * @return true if the user was created, otherwise false
     */
    @Transactional
    public boolean createUser(@NonNull AuthUser user) {
        String normalizedEmail = normalize(user.email());
        boolean exists = authUserRepository.findByEmail(normalizedEmail).isPresent();
        if (exists) {
            return false;
        }
        AuthUserEntity entity = AuthUserEntity.createNew(normalizedEmail, user.passwordHash());
        authUserRepository.save(entity);
        return true;
    }

    /**
     * Clears all users from the in-memory store.
     */
    @Transactional
    public void clear() {
        authUserRepository.deleteAll();
    }

    private String normalize(@NonNull String email) {
        return email.trim().toLowerCase();
    }
}
