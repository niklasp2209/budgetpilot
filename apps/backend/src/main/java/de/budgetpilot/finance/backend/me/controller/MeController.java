package de.budgetpilot.finance.backend.me.controller;

import de.budgetpilot.finance.backend.me.dto.ChangePasswordRequest;
import de.budgetpilot.finance.backend.me.dto.MeResponse;
import de.budgetpilot.finance.backend.me.dto.MyOrganizationResponse;
import de.budgetpilot.finance.backend.me.service.MeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Niklas Petermeier
 * @since 06.05.2026
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/me")
public class MeController {
    private final MeService meService;

    /**
     * Returns the authenticated user representation.
     *
     * @param jwt authenticated JWT
     * @return me response
     */
    @GetMapping
    public @NonNull MeResponse me(@AuthenticationPrincipal @NonNull Jwt jwt) {
        return meService.me(extractEmail(jwt));
    }

    /**
     * Returns organizations of the authenticated user.
     *
     * @param jwt authenticated JWT
     * @return organizations
     */
    @GetMapping("/organizations")
    public @NonNull List<MyOrganizationResponse> myOrganizations(@AuthenticationPrincipal @NonNull Jwt jwt) {
        return meService.myOrganizations(extractEmail(jwt));
    }

    @PutMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    /**
     * Changes the authenticated user's password.
     *
     * @param request password change payload
     * @param jwt authenticated JWT
     */
    public void changePassword(
            @Valid @RequestBody @NonNull ChangePasswordRequest request,
            @AuthenticationPrincipal @NonNull Jwt jwt
    ) {
        meService.changePassword(extractEmail(jwt), request);
    }

    private @NonNull String extractEmail(@NonNull Jwt jwt) {
        String subject = jwt.getSubject();
        if (subject == null || subject.isBlank()) {
            throw new IllegalStateException("JWT subject is missing.");
        }
        return subject;
    }
}

