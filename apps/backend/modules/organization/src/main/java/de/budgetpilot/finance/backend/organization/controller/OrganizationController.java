package de.budgetpilot.finance.backend.organization.controller;

import de.budgetpilot.finance.backend.organization.dto.CreateOrganizationRequest;
import de.budgetpilot.finance.backend.organization.dto.OrganizationMemberResponse;
import de.budgetpilot.finance.backend.organization.dto.OrganizationResponse;
import de.budgetpilot.finance.backend.organization.dto.UpdateMemberRoleRequest;
import de.budgetpilot.finance.backend.organization.mapper.OrganizationMapper;
import de.budgetpilot.finance.backend.organization.service.OrganizationService;
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
@RequestMapping("/api/v1/organizations")
public class OrganizationController {
    private final OrganizationService organizationService;
    private final OrganizationMapper organizationMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    /**
     * Creates a new organization for the authenticated user.
     *
     * @param request organization creation request
     * @param jwt authenticated JWT token
     * @return created organization response
     */
    public @NonNull OrganizationResponse createOrganization(
        @Valid @RequestBody @NonNull CreateOrganizationRequest request,
        @AuthenticationPrincipal @NonNull Jwt jwt
    ) {
        String email = extractEmail(jwt);
        return organizationMapper.toOrganizationResponse(
            organizationService.createOrganization(request, email)
        );
    }

    @GetMapping("/{organizationId}")
    /**
     * Returns one organization visible to the authenticated member.
     *
     * @param organizationId organization identifier
     * @param jwt authenticated JWT token
     * @return organization response
     */
    public @NonNull OrganizationResponse getOrganization(
        @PathVariable @NonNull UUID organizationId,
        @AuthenticationPrincipal @NonNull Jwt jwt
    ) {
        String email = extractEmail(jwt);
        return organizationMapper.toOrganizationResponse(
            organizationService.getOrganization(organizationId, email)
        );
    }

    @GetMapping("/{organizationId}/members")
    /**
     * Returns all members of one organization.
     *
     * @param organizationId organization identifier
     * @param jwt authenticated JWT token
     * @return list of organization members
     */
    public @NonNull List<OrganizationMemberResponse> getMembers(
        @PathVariable @NonNull UUID organizationId,
        @AuthenticationPrincipal @NonNull Jwt jwt
    ) {
        String email = extractEmail(jwt);
        return organizationService.getMembers(organizationId, email).stream()
            .map(organizationMapper::toOrganizationMemberResponse)
            .toList();
    }

    @PatchMapping("/{organizationId}/members/{userId}/role")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    /**
     * Updates a member role.
     *
     * @param organizationId organization identifier
     * @param userId target member user identifier
     * @param request role update payload
     * @param jwt authenticated JWT token
     */
    public void updateMemberRole(
            @PathVariable @NonNull UUID organizationId,
            @PathVariable @NonNull UUID userId,
            @Valid @RequestBody @NonNull UpdateMemberRoleRequest request,
            @AuthenticationPrincipal @NonNull Jwt jwt
    ) {
        organizationService.updateMemberRole(organizationId, userId, request, extractEmail(jwt));
    }

    @DeleteMapping("/{organizationId}/members/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    /**
     * Removes a member from an organization.
     *
     * @param organizationId organization identifier
     * @param userId target member user identifier
     * @param jwt authenticated JWT token
     */
    public void removeMember(
            @PathVariable @NonNull UUID organizationId,
            @PathVariable @NonNull UUID userId,
            @AuthenticationPrincipal @NonNull Jwt jwt
    ) {
        organizationService.removeMember(organizationId, userId, extractEmail(jwt));
    }

    /**
     * Extracts email from JWT subject.
     *
     * @param jwt authenticated JWT token
     * @return subject email
     */
    private @NonNull String extractEmail(@NonNull Jwt jwt) {
        String subject = jwt.getSubject();
        if (subject == null || subject.isBlank()) {
            throw new IllegalStateException("JWT subject is missing.");
        }
        return subject;
    }
}
