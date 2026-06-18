package de.budgetpilot.finance.backend.organization.authorization;

import de.budgetpilot.finance.backend.auth.domain.AuthUserEntity;
import de.budgetpilot.finance.backend.auth.repository.AuthUserRepository;
import de.budgetpilot.finance.backend.organization.domain.OrganizationMembershipEntity;
import de.budgetpilot.finance.backend.organization.exception.OrganizationAccessDeniedException;
import de.budgetpilot.finance.backend.organization.repository.OrganizationMembershipRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * Central organization authorization checks.
 *
 * @author Niklas Petermeier
 * @since 11.05.2026
 */
@Service
@RequiredArgsConstructor
public class OrganizationAuthorizationService {
    private final AuthUserRepository authUserRepository;
    private final OrganizationMembershipRepository organizationMembershipRepository;
    private final OrganizationPermissionResolver organizationPermissionResolver;

    /**
     * Resolves access context for an organization member.
     *
     * @param organizationId organization identifier
     * @param authenticatedEmail authenticated requester email
     * @return access context
     */
    public @NonNull OrganizationAccessContext resolveAccess(
            @NonNull UUID organizationId,
            @NonNull String authenticatedEmail
    ) {
        AuthUserEntity user = findUserByEmail(authenticatedEmail);
        OrganizationMembershipEntity membership = organizationMembershipRepository
                .findByIdOrganizationIdAndIdUserId(organizationId, user.getId())
                .orElseThrow(() -> new OrganizationAccessDeniedException("Organization access denied."));

        Set<OrganizationPermission> permissions = organizationPermissionResolver.resolve(membership.getRole());
        return new OrganizationAccessContext(user.getId(), organizationId, membership.getRole(), permissions);
    }

    /**
     * Ensures that the requester has one permission.
     *
     * @param organizationId organization identifier
     * @param authenticatedEmail authenticated requester email
     * @param permission required permission
     * @return access context
     */
    public @NonNull OrganizationAccessContext requirePermission(
            @NonNull UUID organizationId,
            @NonNull String authenticatedEmail,
            @NonNull OrganizationPermission permission
    ) {
        OrganizationAccessContext context = resolveAccess(organizationId, authenticatedEmail);
        if (!context.hasPermission(permission)) {
            throw new OrganizationAccessDeniedException("Organization access denied.");
        }
        return context;
    }

    private @NonNull AuthUserEntity findUserByEmail(@NonNull String email) {
        return authUserRepository.findByEmail(normalizeEmail(email))
                .orElseThrow(() -> new OrganizationAccessDeniedException("Authenticated user was not found."));
    }

    private @NonNull String normalizeEmail(@NonNull String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
