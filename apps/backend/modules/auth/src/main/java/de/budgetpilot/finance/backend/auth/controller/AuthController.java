package de.budgetpilot.finance.backend.auth.controller;

import de.budgetpilot.finance.backend.auth.dto.AuthTokensResponse;
import de.budgetpilot.finance.backend.auth.dto.LoginRequest;
import de.budgetpilot.finance.backend.auth.dto.RefreshRequest;
import de.budgetpilot.finance.backend.auth.dto.RegisterRequest;
import de.budgetpilot.finance.backend.auth.mapper.AuthMapper;
import de.budgetpilot.finance.backend.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * @author Niklas Petermeier
 * @since 05.05.2026
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;
    private final AuthMapper authMapper;

    /**
     * Registers a new user and returns an initial token pair.
     *
     * @param request registration payload
     * @return issued access and refresh tokens
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public @NonNull AuthTokensResponse register(@Valid @RequestBody @NonNull RegisterRequest request) {
        return authMapper.toResponse(authService.register(request));
    }

    /**
     * Authenticates an existing user and returns a token pair.
     *
     * @param request login payload
     * @return issued access and refresh tokens
     */
    @PostMapping("/login")
    public @NonNull AuthTokensResponse login(@Valid @RequestBody @NonNull LoginRequest request) {
        return authMapper.toResponse(authService.login(request));
    }

    /**
     * Exchanges a valid refresh token for a new token pair.
     *
     * @param request refresh payload
     * @return newly issued access and refresh tokens
     */
    @PostMapping("/refresh")
    public @NonNull AuthTokensResponse refresh(@Valid @RequestBody @NonNull RefreshRequest request) {
        return authMapper.toResponse(authService.refresh(request));
    }
}
