package de.budgetpilot.finance.backend.auth.service;

import de.budgetpilot.finance.backend.auth.domain.AuthUser;
import de.budgetpilot.finance.backend.auth.domain.TokenPair;
import de.budgetpilot.finance.backend.auth.dto.LoginRequest;
import de.budgetpilot.finance.backend.auth.dto.RefreshRequest;
import de.budgetpilot.finance.backend.auth.dto.RegisterRequest;
import de.budgetpilot.finance.backend.auth.exception.EmailAlreadyExistsException;
import de.budgetpilot.finance.backend.auth.exception.InvalidCredentialsException;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author Niklas Petermeier
 * @since 05.05.2026
 */
@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthUserStore authUserStore;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    /**
     * Registers a user and issues a new token pair.
     *
     * @param request registration payload
     * @return issued access and refresh tokens
     */
    public @NonNull TokenPair register(@NonNull RegisterRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();
        String passwordHash = Objects.requireNonNull(
                passwordEncoder.encode(request.password()),
                "Password hash must not be null."
        );

        boolean created = authUserStore.createUser(new AuthUser(normalizedEmail, passwordHash));
        if (!created) {
            throw new EmailAlreadyExistsException("Email is already registered.");
        }

        return tokenService.issueTokenPair(normalizedEmail);
    }

    /**
     * Authenticates a user and issues a new token pair.
     *
     * @param request login payload
     * @return issued access and refresh tokens
     */
    public @NonNull TokenPair login(@NonNull LoginRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();
        AuthUser user = authUserStore.findByEmail(normalizedEmail)
                .orElseThrow(() -> new InvalidCredentialsException("Email or password is invalid."));

        if (!passwordEncoder.matches(request.password(), user.passwordHash())) {
            throw new InvalidCredentialsException("Email or password is invalid.");
        }

        return tokenService.issueTokenPair(user.email());
    }

    /**
     * Creates a new token pair using a refresh token.
     *
     * @param request refresh payload
     * @return newly issued access and refresh tokens
     */
    public @NonNull TokenPair refresh(@NonNull RefreshRequest request) {
        String email = tokenService.validateAndExtractRefreshSubject(request.refreshToken());
        return authUserStore.findByEmail(email)
                .map(user -> tokenService.issueTokenPair(user.email()))
                .orElseThrow(() -> new InvalidCredentialsException("Email or password is invalid."));
    }
}
