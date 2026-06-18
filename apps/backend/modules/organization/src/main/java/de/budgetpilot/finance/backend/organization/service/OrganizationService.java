package de.budgetpilot.finance.backend.organization.service;

import de.budgetpilot.finance.backend.auth.domain.AuthUserEntity;
import de.budgetpilot.finance.backend.auth.repository.AuthUserRepository;
import de.budgetpilot.finance.backend.organization.authorization.OrganizationAccessContext;
import de.budgetpilot.finance.backend.organization.authorization.OrganizationAuthorizationService;
import de.budgetpilot.finance.backend.organization.authorization.OrganizationPermission;
import de.budgetpilot.finance.backend.organization.domain.MembershipRole;
import de.budgetpilot.finance.backend.organization.domain.OrganizationEntity;
import de.budgetpilot.finance.backend.organization.domain.OrganizationMembershipEntity;
import de.budgetpilot.finance.backend.organization.dto.CreateOrganizationRequest;
import de.budgetpilot.finance.backend.organization.dto.UpdateMemberRoleRequest;
import de.budgetpilot.finance.backend.organization.exception.OrganizationAccessDeniedException;
import de.budgetpilot.finance.backend.organization.exception.OrganizationMemberNotFoundException;
import de.budgetpilot.finance.backend.organization.exception.OrganizationMemberOperationException;
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
    private final OrganizationAuthorizationService organizationAuthorizationService;

    /**
     * Creates a new organization and owner membership.
     *
     * @param request organization creation request
     * @param authenticatedEmail authenticated requester email
     * @return created organization entity
     */
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

    /**
     * Returns one organization if requester is member.
     *
     * @param organizationId organization identifier
     * @param authenticatedEmail authenticated requester email
     * @return organization entity
     */
    @Transactional(readOnly = true)
    public @NonNull OrganizationEntity getOrganization(
            @NonNull UUID organizationId,
            @NonNull String authenticatedEmail
    ) {
        organizationAuthorizationService.requirePermission(
                organizationId, authenticatedEmail, OrganizationPermission.ORGANIZATION_READ
        );
        return organizationRepository.findById(organizationId)
                .orElseThrow(() -> new OrganizationNotFoundException("Organization not found."));
    }

    /**
     * Returns members for an organization if requester is member.
     *
     * @param organizationId organization identifier
     * @param authenticatedEmail authenticated requester email
     * @return membership entities
     */
    @Transactional(readOnly = true)
    public @NonNull List<OrganizationMembershipEntity> getMembers(
            @NonNull UUID organizationId,
            @NonNull String authenticatedEmail
    ) {
        organizationAuthorizationService.requirePermission(
                organizationId, authenticatedEmail, OrganizationPermission.ORGANIZATION_READ
        );
        return organizationMembershipRepository.findByIdOrganizationId(organizationId);
    }

    /**
     * Updates a member role in an organization.
     *
     * @param organizationId organization identifier
     * @param targetUserId target member user identifier
     * @param request role update request
     * @param authenticatedEmail authenticated requester email
     */
    @Transactional
    public void updateMemberRole(
            @NonNull UUID organizationId,
            @NonNull UUID targetUserId,
            @NonNull UpdateMemberRoleRequest request,
            @NonNull String authenticatedEmail
    ) {
        OrganizationAccessContext requesterContext = organizationAuthorizationService.requirePermission(
                organizationId, authenticatedEmail, OrganizationPermission.MEMBERS_MANAGE
        );
        OrganizationMembershipEntity targetMembership = organizationMembershipRepository
                .findByIdOrganizationIdAndIdUserId(organizationId, targetUserId)
                .orElseThrow(() -> new OrganizationMemberNotFoundException("Member not found."));

        MembershipRole requesterRole = requesterContext.role();
        MembershipRole targetRole = targetMembership.getRole();
        MembershipRole newRole = request.role();

        if (newRole == MembershipRole.OWNER) {
            throw new OrganizationMemberOperationException("Cannot assign OWNER role.");
        }
        if (targetRole == MembershipRole.OWNER) {
            throw new OrganizationMemberOperationException("Cannot change role of OWNER.");
        }
        if (requesterContext.userId().equals(targetUserId)) {
            throw new OrganizationMemberOperationException("Cannot change own role.");
        }
        if (requesterRole == MembershipRole.ADMIN && newRole == MembershipRole.ADMIN) {
            throw new OrganizationAccessDeniedException("Organization access denied.");
        }

        targetMembership.setRole(newRole);
        organizationMembershipRepository.save(targetMembership);
    }

    /**
     * Removes a member from an organization.
     *
     * @param organizationId organization identifier
     * @param targetUserId target member user identifier
     * @param authenticatedEmail authenticated requester email
     */
    @Transactional
    public void removeMember(
            @NonNull UUID organizationId,
            @NonNull UUID targetUserId,
            @NonNull String authenticatedEmail
    ) {
        OrganizationAccessContext requesterContext = organizationAuthorizationService.requirePermission(
                organizationId, authenticatedEmail, OrganizationPermission.MEMBERS_MANAGE
        );
        OrganizationMembershipEntity targetMembership = organizationMembershipRepository
                .findByIdOrganizationIdAndIdUserId(organizationId, targetUserId)
                .orElseThrow(() -> new OrganizationMemberNotFoundException("Member not found."));

        MembershipRole requesterRole = requesterContext.role();
        MembershipRole targetRole = targetMembership.getRole();

        if (targetRole == MembershipRole.OWNER) {
            throw new OrganizationMemberOperationException("Cannot remove OWNER.");
        }
        if (requesterRole == MembershipRole.ADMIN && targetRole == MembershipRole.ADMIN) {
            throw new OrganizationAccessDeniedException("Organization access denied.");
        }

        organizationMembershipRepository.delete(targetMembership);
    }

    /**
     * Resolves a user by normalized email.
     *
     * @param email raw email value
     * @return resolved auth user entity
     */
    private @NonNull AuthUserEntity findUserByEmail(@NonNull String email) {
        return authUserRepository.findByEmail(email.trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new OrganizationAccessDeniedException("Authenticated user was not found."));
    }

    /**
     * Normalizes slug values.
     *
     * @param slug raw slug
     * @return normalized slug
     */
    private @NonNull String normalizeSlug(@NonNull String slug) {
        return slug.trim().toLowerCase(Locale.ROOT);
    }
}
