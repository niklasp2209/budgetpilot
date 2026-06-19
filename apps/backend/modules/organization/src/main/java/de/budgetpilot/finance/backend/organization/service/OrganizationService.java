package de.budgetpilot.finance.backend.organization.service;

import de.budgetpilot.finance.backend.auth.domain.AuthUserEntity;
import de.budgetpilot.finance.backend.auth.repository.AuthUserRepository;
import de.budgetpilot.finance.backend.auth.service.AuthUserStore;
import de.budgetpilot.finance.backend.organization.authorization.OrganizationAccessContext;
import de.budgetpilot.finance.backend.organization.authorization.OrganizationAuthorizationService;
import de.budgetpilot.finance.backend.organization.authorization.OrganizationPermission;
import de.budgetpilot.finance.backend.organization.domain.MembershipRole;
import de.budgetpilot.finance.backend.organization.domain.OrganizationCurrencies;
import de.budgetpilot.finance.backend.organization.domain.OrganizationEntity;
import de.budgetpilot.finance.backend.organization.domain.OrganizationMembershipEntity;
import de.budgetpilot.finance.backend.organization.dto.AddOrganizationMemberRequest;
import de.budgetpilot.finance.backend.organization.dto.CreateOrganizationRequest;
import de.budgetpilot.finance.backend.organization.dto.OrganizationMemberResponse;
import de.budgetpilot.finance.backend.organization.dto.UpdateMemberRoleRequest;
import de.budgetpilot.finance.backend.organization.dto.UpdateOrganizationRequest;
import de.budgetpilot.finance.backend.organization.event.OrganizationCurrencyChangedEvent;
import de.budgetpilot.finance.backend.organization.exception.OrganizationAccessDeniedException;
import de.budgetpilot.finance.backend.organization.exception.OrganizationMemberNotFoundException;
import de.budgetpilot.finance.backend.organization.exception.OrganizationMemberOperationException;
import de.budgetpilot.finance.backend.organization.exception.OrganizationNotFoundException;
import de.budgetpilot.finance.backend.organization.exception.OrganizationSlugAlreadyExistsException;
import de.budgetpilot.finance.backend.organization.mapper.OrganizationMapper;
import de.budgetpilot.finance.backend.organization.repository.OrganizationMemberPermissionGroupRepository;
import de.budgetpilot.finance.backend.organization.repository.OrganizationMembershipRepository;
import de.budgetpilot.finance.backend.organization.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
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
    private final OrganizationMemberPermissionGroupRepository organizationMemberPermissionGroupRepository;
    private final AuthUserRepository authUserRepository;
    private final AuthUserStore authUserStore;
    private final OrganizationAuthorizationService organizationAuthorizationService;
    private final OrganizationMapper organizationMapper;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher applicationEventPublisher;

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
        String currency = OrganizationCurrencies.normalizeOrDefault(request.currency());
        OrganizationEntity organization = organizationRepository.save(
                OrganizationEntity.createNew(request.name().trim(), normalizedSlug, requester.getId(), currency)
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

    @Transactional(readOnly = true)
    public @NonNull List<OrganizationMemberResponse> listMembers(
            @NonNull UUID organizationId,
            @NonNull String authenticatedEmail
    ) {
        organizationAuthorizationService.requirePermission(
                organizationId, authenticatedEmail, OrganizationPermission.ORGANIZATION_READ
        );
        List<OrganizationMemberResponse> members = new ArrayList<>();
        for (OrganizationMembershipEntity membership : organizationMembershipRepository.findByIdOrganizationId(organizationId)) {
            UUID userId = membership.getId().getUserId();
            AuthUserEntity user = authUserRepository.findById(userId)
                    .orElseThrow(() -> new IllegalStateException("Member user was not found."));
            List<UUID> groupIds = organizationMemberPermissionGroupRepository
                    .findByIdOrganizationIdAndIdUserId(organizationId, userId)
                    .stream()
                    .map(assignment -> assignment.getId().getGroupId())
                    .toList();
            members.add(organizationMapper.toOrganizationMemberResponse(
                    membership, user.getEmail(), new HashSet<>(groupIds)
            ));
        }
        return List.copyOf(members);
    }

    /**
     * Adds a member directly by email, creating a user account when needed.
     *
     * @param organizationId organization identifier
     * @param request add member payload
     * @param authenticatedEmail authenticated requester email
     * @return created or existing member response
     */
    @Transactional
    public @NonNull OrganizationMemberResponse addMember(
            @NonNull UUID organizationId,
            @NonNull AddOrganizationMemberRequest request,
            @NonNull String authenticatedEmail
    ) {
        OrganizationAccessContext requesterContext = organizationAuthorizationService.requirePermission(
                organizationId, authenticatedEmail, OrganizationPermission.MEMBERS_MANAGE
        );

        MembershipRole newRole = request.role();
        if (newRole == MembershipRole.OWNER) {
            throw new OrganizationMemberOperationException("Cannot assign OWNER role.");
        }
        if (requesterContext.role() == MembershipRole.ADMIN && newRole == MembershipRole.ADMIN) {
            throw new OrganizationAccessDeniedException("Organization access denied.");
        }

        String normalizedEmail = request.email().trim().toLowerCase(Locale.ROOT);
        AuthUserEntity user = authUserRepository.findByEmail(normalizedEmail).orElse(null);
        if (user == null) {
            String password = request.password();
            if (password == null || password.isBlank()) {
                throw new OrganizationMemberOperationException("Password is required for new users.");
            }
            String passwordHash = Objects.requireNonNull(
                    passwordEncoder.encode(password),
                    "Password hash must not be null."
            );
            user = authUserStore.createUser(normalizedEmail, passwordHash)
                    .map(created -> authUserRepository.findById(created.id())
                            .orElseThrow(() -> new IllegalStateException("Created user was not found.")))
                    .orElseThrow(() -> new OrganizationMemberOperationException("Email is already registered."));
        }

        if (organizationMembershipRepository.findByIdOrganizationIdAndIdUserId(organizationId, user.getId()).isPresent()) {
            throw new OrganizationMemberOperationException("User is already a member.");
        }

        OrganizationMembershipEntity membership = organizationMembershipRepository.save(
                OrganizationMembershipEntity.createNew(organizationId, user.getId(), newRole)
        );
        return organizationMapper.toOrganizationMemberResponse(membership, user.getEmail(), Set.of());
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
     * Updates organization name and slug.
     *
     * @param organizationId organization identifier
     * @param request organization update payload
     * @param authenticatedEmail authenticated requester email
     * @return updated organization entity
     */
    @Transactional
    public @NonNull OrganizationEntity updateOrganization(
            @NonNull UUID organizationId,
            @NonNull UpdateOrganizationRequest request,
            @NonNull String authenticatedEmail
    ) {
        OrganizationAccessContext requesterContext = organizationAuthorizationService.requirePermission(
                organizationId, authenticatedEmail, OrganizationPermission.ORGANIZATION_READ
        );
        if (requesterContext.role() != MembershipRole.OWNER && requesterContext.role() != MembershipRole.ADMIN) {
            throw new OrganizationAccessDeniedException("Organization access denied.");
        }

        OrganizationEntity organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new OrganizationNotFoundException("Organization not found."));

        String normalizedSlug = normalizeSlug(request.slug());
        if (organizationRepository.findBySlugAndIdNot(normalizedSlug, organizationId).isPresent()) {
            throw new OrganizationSlugAlreadyExistsException("Organization slug already exists.");
        }

        organization.setName(request.name().trim());
        organization.setSlug(normalizedSlug);
        String previousCurrency = organization.getCurrency();
        String newCurrency = OrganizationCurrencies.normalize(request.currency());
        organization.setCurrency(newCurrency);
        OrganizationEntity saved = organizationRepository.save(organization);
        if (!previousCurrency.equals(newCurrency)) {
            applicationEventPublisher.publishEvent(
                    new OrganizationCurrencyChangedEvent(organizationId, newCurrency)
            );
        }
        return saved;
    }

    /**
     * Deletes an organization and all related data.
     *
     * @param organizationId organization identifier
     * @param authenticatedEmail authenticated requester email
     */
    @Transactional
    public void deleteOrganization(@NonNull UUID organizationId, @NonNull String authenticatedEmail) {
        OrganizationAccessContext requesterContext = organizationAuthorizationService.requirePermission(
                organizationId, authenticatedEmail, OrganizationPermission.ORGANIZATION_READ
        );
        if (requesterContext.role() != MembershipRole.OWNER) {
            throw new OrganizationAccessDeniedException("Organization access denied.");
        }

        OrganizationEntity organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new OrganizationNotFoundException("Organization not found."));
        organizationRepository.delete(organization);
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
