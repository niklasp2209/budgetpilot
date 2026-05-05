package de.budgetpilot.finance.backend.auth.service;

import de.budgetpilot.finance.backend.auth.domain.AuthUser;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Niklas Petermeier
 * @since 05.05.2026
 */
@Component
public class AuthUserStore {
    private final ConcurrentMap<String, AuthUser> usersByEmail = new ConcurrentHashMap<>();

    /**
     * Finds a user by email.
     *
     * @param email user email address
     * @return optional user if present
     */
    public @NonNull Optional<AuthUser> findByEmail(@NonNull String email) {
        return Optional.ofNullable(usersByEmail.get(normalize(email)));
    }

    /**
     * Creates a user if the email is not already present.
     *
     * @param user user to store
     * @return true if the user was created, otherwise false
     */
    public boolean createUser(@NonNull AuthUser user) {
        return usersByEmail.putIfAbsent(normalize(user.email()), user) == null;
    }

    /**
     * Clears all users from the in-memory store.
     */
    public void clear() {
        usersByEmail.clear();
    }

    private String normalize(@NonNull String email) {
        return email.trim().toLowerCase();
    }
}
