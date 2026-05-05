package de.budgetpilot.finance.backend.organization.service;

import de.budgetpilot.finance.backend.auth.domain.AuthUserEntity;
import de.budgetpilot.finance.backend.auth.repository.AuthUserRepository;
import de.budgetpilot.finance.backend.organization.domain.MembershipRole;
import de.budgetpilot.finance.backend.organization.domain.OrganizationEntity;
import de.budgetpilot.finance.backend.organization.domain.OrganizationMembershipEntity;
import de.budgetpilot.finance.backend.organization.dto.CreateOrganizationRequest;
import de.budgetpilot.finance.backend.organization.exception.OrganizationAccessDeniedException;
import de.budgetpilot.finance.backend.organization.exception.OrganizationNotFoundException;
import de.budgetpilot.finance.backend.organization.exception.OrganizationSlugAlreadyExistsException;
import de.budgetpilot.finance.backend.organization.repository.OrganizationMembershipRepository;
import de.budgetpilot.finance.backend.organization.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * @author Niklas Petermeier
 * @since 05.05.2026
 */
@Service
@RequiredArgsConstructor
public class OrganizationService {
    private final OrganizationRepository organizationRepository;
    private final OrganizationMembershipRepository organizationMembershipRepository;
    private final AuthUserRepository authUserRepository;

    @Transactional
    public @NonNull OrganizationEntity createOrganization(
            @NonNull CreateOrganizationRequest request,
            @NonNull String authenticatedEmail
    ) {
        String normalizedSlug = normalizeSlug(request.slug());
        if (organizationRepository.findBySlug(normalizedSlug).isPresent()) {
            throw new OrganizationSlugAlreadyExistsException("Organization slug already exists.");
        }

        AuthUserEntity requester = findUserByEmail(authenticatedEmail);
        OrganizationEntity organization = organizationRepository.save(
                OrganizationEntity.createNew(request.name().trim(), normalizedSlug, requester.getId())
        );

        organizationMembershipRepository.save(
                OrganizationMembershipEntity.createNew(organization.getId(), requester.getId(), MembershipRole.OWNER)
        );

        return organization;
    }

    @Transactional(readOnly = true)
    public @NonNull OrganizationEntity getOrganization(
            @NonNull UUID organizationId,
            @NonNull String authenticatedEmail
    ) {
        AuthUserEntity requester = findUserByEmail(authenticatedEmail);
        ensureMembership(organizationId, requester.getId());
        return organizationRepository.findById(organizationId)
                .orElseThrow(() -> new OrganizationNotFoundException("Organization not found."));
    }

    @Transactional(readOnly = true)
    public @NonNull List<OrganizationMembershipEntity> getMembers(
            @NonNull UUID organizationId,
            @NonNull String authenticatedEmail
    ) {
        AuthUserEntity requester = findUserByEmail(authenticatedEmail);
        ensureMembership(organizationId, requester.getId());
        return organizationMembershipRepository.findByIdOrganizationId(organizationId);
    }

    private @NonNull AuthUserEntity findUserByEmail(@NonNull String email) {
        return authUserRepository.findByEmail(email.trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new OrganizationAccessDeniedException("Authenticated user was not found."));
    }

    private void ensureMembership(@NonNull UUID organizationId, @NonNull UUID userId) {
        boolean isMember = organizationMembershipRepository
                .findByIdOrganizationIdAndIdUserId(organizationId, userId)
                .isPresent();
        if (!isMember) {
            throw new OrganizationAccessDeniedException("Organization access denied.");
        }
    }

    private @NonNull String normalizeSlug(@NonNull String slug) {
        return slug.trim().toLowerCase(Locale.ROOT);
    }
}
