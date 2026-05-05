package de.budgetpilot.finance.backend.auth.domain;

import org.jspecify.annotations.NonNull;

/**
 * @author Niklas Petermeier
 * @since 05.05.2026
 */
public record AuthUser(
        @NonNull String email,
        @NonNull String passwordHash
) {
}
