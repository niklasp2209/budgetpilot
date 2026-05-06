package de.budgetpilot.finance.backend.me.dto;

import org.jspecify.annotations.NonNull;

import java.util.UUID;

/**
 * @author Niklas Petermeier
 * @since 06.05.2026
 */
public record MeResponse(
        @NonNull UUID id,
        @NonNull String email
) {
}

