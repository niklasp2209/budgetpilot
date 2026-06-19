package de.budgetpilot.finance.backend.me.service;

import de.budgetpilot.finance.backend.auth.domain.AuthUserEntity;
import de.budgetpilot.finance.backend.auth.exception.InvalidCredentialsException;
import de.budgetpilot.finance.backend.auth.repository.AuthUserRepository;
import de.budgetpilot.finance.backend.me.dto.ChangePasswordRequest;
import de.budgetpilot.finance.backend.me.dto.MeResponse;
import de.budgetpilot.finance.backend.me.dto.MyOrganizationResponse;
import de.budgetpilot.finance.backend.organization.authorization.OrganizationAccessContext;
import de.budgetpilot.finance.backend.organization.authorization.OrganizationAuthorizationService;
import de.budgetpilot.finance.backend.organization.domain.OrganizationEntity;
import de.budgetpilot.finance.backend.organization.domain.OrganizationMembershipEntity;
import de.budgetpilot.finance.backend.organization.repository.OrganizationMembershipRepository;
import de.budgetpilot.finance.backend.organization.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.Objects;

/**
 * @author Niklas Petermeier
 * @since 06.05.2026
 */
@Service
@RequiredArgsConstructor
public class MeService {
    private final AuthUserRepository authUserRepository;
    private final OrganizationMembershipRepository organizationMembershipRepository;
    private final OrganizationRepository organizationRepository;
    private final OrganizationAuthorizationService organizationAuthorizationService;
    private final PasswordEncoder passwordEncoder;

    /**
     * Returns the authenticated user representation.
     *
     * @param authenticatedEmail authenticated email
     * @return me response
     */
    @Transactional(readOnly = true)
    public @NonNull MeResponse me(@NonNull String authenticatedEmail) {
        AuthUserEntity user = authUserRepository.findByEmail(normalizeEmail(authenticatedEmail))
                .orElseThrow(() -> new IllegalStateException("Authenticated user was not found."));
        return new MeResponse(user.getId(), user.getEmail());
    }

    /**
     * Returns organizations of the authenticated user.
     *
     * @param authenticatedEmail authenticated email
     * @return list of organizations with membership role
     */
    @Transactional(readOnly = true)
    public @NonNull List<MyOrganizationResponse> myOrganizations(@NonNull String authenticatedEmail) {
        AuthUserEntity user = authUserRepository.findByEmail(normalizeEmail(authenticatedEmail))
                .orElseThrow(() -> new IllegalStateException("Authenticated user was not found."));

        List<OrganizationMembershipEntity> memberships = organizationMembershipRepository.findByIdUserId(user.getId());
        if (memberships.isEmpty()) {
            return List.of();
        }

        Set<UUID> organizationIds = memberships.stream()
                .map(membership -> membership.getId().getOrganizationId())
                .collect(java.util.stream.Collectors.toSet());

        Map<UUID, OrganizationEntity> organizationsById = new HashMap<>();
        for (OrganizationEntity organization : organizationRepository.findAllById(organizationIds)) {
            organizationsById.put(organization.getId(), organization);
        }

        List<MyOrganizationResponse> result = new ArrayList<>();
        for (OrganizationMembershipEntity membership : memberships) {
            UUID organizationId = membership.getId().getOrganizationId();
            OrganizationEntity organization = organizationsById.get(organizationId);
            if (organization == null) {
                continue;
            }
            OrganizationAccessContext accessContext = organizationAuthorizationService.resolveAccess(
                    organizationId, user.getEmail()
            );
            result.add(new MyOrganizationResponse(
                    organization.getId(),
                    organization.getName(),
                    organization.getSlug(),
                    organization.getCurrency(),
                    membership.getRole(),
                    accessContext.permissions()
            ));
        }

        result.sort(Comparator.comparing(MyOrganizationResponse::slug));
        return List.copyOf(result);
    }

    /**
     * Changes the authenticated user's password.
     *
     * @param authenticatedEmail authenticated email
     * @param request password change payload
     */
    @Transactional
    public void changePassword(@NonNull String authenticatedEmail, @NonNull ChangePasswordRequest request) {
        AuthUserEntity user = authUserRepository.findByEmail(normalizeEmail(authenticatedEmail))
                .orElseThrow(() -> new IllegalStateException("Authenticated user was not found."));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Current password is invalid.");
        }

        user.setPasswordHash(Objects.requireNonNull(
                passwordEncoder.encode(request.newPassword()),
                "Password hash must not be null."
        ));
        authUserRepository.save(user);
    }

    private @NonNull String normalizeEmail(@NonNull String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}

