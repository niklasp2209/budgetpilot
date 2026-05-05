package de.budgetpilot.finance.backend.organization.controller;

import de.budgetpilot.finance.backend.organization.dto.CreateOrganizationRequest;
import de.budgetpilot.finance.backend.organization.dto.OrganizationMemberResponse;
import de.budgetpilot.finance.backend.organization.dto.OrganizationResponse;
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
    public @NonNull List<OrganizationMemberResponse> getMembers(
        @PathVariable @NonNull UUID organizationId,
        @AuthenticationPrincipal @NonNull Jwt jwt
    ) {
        String email = extractEmail(jwt);
        return organizationService.getMembers(organizationId, email).stream()
            .map(organizationMapper::toOrganizationMemberResponse)
            .toList();
    }

    private @NonNull String extractEmail(@NonNull Jwt jwt) {
        String subject = jwt.getSubject();
        if (subject == null || subject.isBlank()) {
            throw new IllegalStateException("JWT subject is missing.");
        }
        return subject;
    }
}
