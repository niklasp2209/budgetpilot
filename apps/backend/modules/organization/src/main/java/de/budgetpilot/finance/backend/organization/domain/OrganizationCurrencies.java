package de.budgetpilot.finance.backend.organization.domain;

import de.budgetpilot.finance.backend.organization.exception.OrganizationCurrencyException;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Locale;
import java.util.Set;

/**
 * @author Niklas Petermeier
 * @since 19.06.2026
 */
public final class OrganizationCurrencies {
    public static final String DEFAULT = "EUR";

    private static final Set<String> ALLOWED = Set.of(
            "EUR", "USD", "GBP", "CHF", "JPY", "CAD", "AUD", "SEK", "NOK", "DKK", "PLN", "CZK"
    );

    private OrganizationCurrencies() {
    }

    public static @NonNull String normalize(@NonNull String currency) {
        String normalized = currency.trim().toUpperCase(Locale.ROOT);
        if (!ALLOWED.contains(normalized)) {
            throw new OrganizationCurrencyException("Unsupported currency.");
        }
        return normalized;
    }

    public static @NonNull String normalizeOrDefault(@Nullable String currency) {
        if (currency == null || currency.isBlank()) {
            return DEFAULT;
        }
        return normalize(currency);
    }
}
