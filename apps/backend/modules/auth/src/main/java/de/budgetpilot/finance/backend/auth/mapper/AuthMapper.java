package de.budgetpilot.finance.backend.auth.mapper;

import de.budgetpilot.finance.backend.auth.domain.TokenPair;
import de.budgetpilot.finance.backend.auth.dto.AuthTokensResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

/**
 * @author Niklas Petermeier
 * @since 05.05.2026
 */
@Component
public class AuthMapper {
    /**
     * Maps domain token data to API response format.
     *
     * @param tokenPair domain token pair
     * @return API response object
     */
    public @NonNull AuthTokensResponse toResponse(@NonNull TokenPair tokenPair) {
        return new AuthTokensResponse(
                tokenPair.accessToken(),
                tokenPair.refreshToken(),
                "Bearer",
                tokenPair.accessExpiresInSeconds()
        );
    }
}
