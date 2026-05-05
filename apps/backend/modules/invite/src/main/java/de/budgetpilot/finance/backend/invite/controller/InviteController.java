package de.budgetpilot.finance.backend.invite.controller;

import de.budgetpilot.finance.backend.invite.dto.CreateInviteRequest;
import de.budgetpilot.finance.backend.invite.dto.InviteResponse;
import de.budgetpilot.finance.backend.invite.mapper.InviteMapper;
import de.budgetpilot.finance.backend.invite.service.InviteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * @author Niklas Petermeier
 * @since 05.05.2026
 */
@RestController
@RequiredArgsConstructor
public class InviteController {
    private final InviteService inviteService;
    private final InviteMapper inviteMapper;

    @PostMapping("/api/v1/organizations/{organizationId}/invites")
    @ResponseStatus(HttpStatus.CREATED)
    /**
     * Creates a new organization invitation.
     *
     * @param organizationId organization identifier
     * @param request invitation creation payload
     * @param jwt authenticated user token
     * @return created invitation response
     */
    public @NonNull InviteResponse createInvite(
            @PathVariable @NonNull UUID organizationId,
            @Valid @RequestBody @NonNull CreateInviteRequest request,
            @AuthenticationPrincipal @NonNull Jwt jwt
    ) {
        return inviteMapper.toResponse(inviteService.createInvite(organizationId, extractEmail(jwt), request));
    }

    @GetMapping("/api/v1/organizations/{organizationId}/invites")
    /**
     * Lists active invitations for an organization.
     *
     * @param organizationId organization identifier
     * @param jwt authenticated user token
     * @return active invitation responses
     */
    public @NonNull List<InviteResponse> listInvites(
            @PathVariable @NonNull UUID organizationId,
            @AuthenticationPrincipal @NonNull Jwt jwt
    ) {
        return inviteService.listInvites(organizationId, extractEmail(jwt)).stream()
                .map(inviteMapper::toResponse)
                .toList();
    }

    @PostMapping("/api/v1/invites/{token}/accept")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    /**
     * Accepts an invite token for the authenticated user.
     *
     * @param token invitation token
     * @param jwt authenticated user token
     */
    public void acceptInvite(
            @PathVariable @NonNull String token,
            @AuthenticationPrincipal @NonNull Jwt jwt
    ) {
        inviteService.acceptInvite(token, extractEmail(jwt));
    }

    @PostMapping("/api/v1/invites/{token}/decline")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    /**
     * Declines an invite token for the authenticated user.
     *
     * @param token invitation token
     * @param jwt authenticated user token
     */
    public void declineInvite(
            @PathVariable @NonNull String token,
            @AuthenticationPrincipal @NonNull Jwt jwt
    ) {
        inviteService.declineInvite(token, extractEmail(jwt));
    }

    /**
     * Extracts the authenticated email from JWT subject.
     *
     * @param jwt authenticated token
     * @return normalized subject email
     */
    private @NonNull String extractEmail(@NonNull Jwt jwt) {
        String subject = jwt.getSubject();
        if (subject == null || subject.isBlank()) {
            throw new IllegalStateException("JWT subject is missing.");
        }
        return subject;
    }
}
