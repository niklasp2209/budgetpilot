package de.budgetpilot.finance.backend.auth.config;

import org.jspecify.annotations.NonNull;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Niklas Petermeier
 * @since 05.05.2026
 */
@ConfigurationProperties(prefix = "auth.jwt")
public record JwtProperties(
        @NonNull String secret,
        long accessTtlSeconds,
        long refreshTtlSeconds
) {
}
