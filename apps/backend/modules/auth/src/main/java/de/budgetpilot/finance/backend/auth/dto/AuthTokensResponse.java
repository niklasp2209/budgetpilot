package de.budgetpilot.finance.backend.auth.dto;

import org.jspecify.annotations.NonNull;

/**
 * @author Niklas Petermeier
 * @since 05.05.2026
 */
public record AuthTokensResponse(
        @NonNull String accessToken,
        @NonNull String refreshToken,
        @NonNull String tokenType,
        long expiresIn
) {
}
