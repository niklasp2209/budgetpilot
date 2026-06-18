package de.budgetpilot.finance.backend.organization.service;

import de.budgetpilot.finance.backend.organization.authorization.OrganizationAuthorizationService;
import de.budgetpilot.finance.backend.organization.authorization.OrganizationPermission;
import de.budgetpilot.finance.backend.organization.domain.OrganizationMemberPermissionGroupEntity;
import de.budgetpilot.finance.backend.organization.domain.OrganizationPermissionGroupEntity;
import de.budgetpilot.finance.backend.organization.dto.AssignMemberPermissionGroupsRequest;
import de.budgetpilot.finance.backend.organization.dto.CreatePermissionGroupRequest;
import de.budgetpilot.finance.backend.organization.dto.UpdatePermissionGroupRequest;
import de.budgetpilot.finance.backend.organization.exception.OrganizationMemberNotFoundException;
import de.budgetpilot.finance.backend.organization.exception.PermissionGroupConflictException;
import de.budgetpilot.finance.backend.organization.exception.PermissionGroupNotFoundException;
import de.budgetpilot.finance.backend.organization.repository.OrganizationMemberPermissionGroupRepository;
import de.budgetpilot.finance.backend.organization.repository.OrganizationMembershipRepository;
import de.budgetpilot.finance.backend.organization.repository.OrganizationPermissionGroupRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * @author Niklas Petermeier
 * @since 18.06.2026
 */
@Service
@RequiredArgsConstructor
public class PermissionGroupService {
    private final OrganizationPermissionGroupRepository organizationPermissionGroupRepository;
    private final OrganizationMemberPermissionGroupRepository organizationMemberPermissionGroupRepository;
    private final OrganizationMembershipRepository organizationMembershipRepository;
    private final OrganizationAuthorizationService organizationAuthorizationService;

    /**
     * Creates a custom permission group for an organization.
     *
     * @param organizationId organization identifier
     * @param authenticatedEmail authenticated requester email
     * @param request creation payload
     * @return created permission group entity
     */
    @Transactional
    public @NonNull OrganizationPermissionGroupEntity createGroup(
            @NonNull UUID organizationId,
            @NonNull String authenticatedEmail,
            @NonNull CreatePermissionGroupRequest request
    ) {
        organizationAuthorizationService.requirePermission(
                organizationId, authenticatedEmail, OrganizationPermission.PERMISSION_GROUPS_MANAGE
        );

        String name = request.name().trim();
        if (organizationPermissionGroupRepository.findByOrganizationIdAndName(organizationId, name).isPresent()) {
            throw new PermissionGroupConflictException("Permission group name already exists.");
        }

        Set<OrganizationPermission> permissions = normalizePermissions(request.permissions());
        return organizationPermissionGroupRepository.save(
                OrganizationPermissionGroupEntity.createNew(organizationId, name, permissions)
        );
    }

    /**
     * Lists all permission groups for an organization.
     *
     * @param organizationId organization identifier
     * @param authenticatedEmail authenticated requester email
     * @return permission groups
     */
    @Transactional(readOnly = true)
    public @NonNull List<OrganizationPermissionGroupEntity> listGroups(
            @NonNull UUID organizationId,
            @NonNull String authenticatedEmail
    ) {
        organizationAuthorizationService.requirePermission(
                organizationId, authenticatedEmail, OrganizationPermission.PERMISSION_GROUPS_MANAGE
        );
        return organizationPermissionGroupRepository.findByOrganizationId(organizationId);
    }

    /**
     * Updates one permission group.
     *
     * @param organizationId organization identifier
     * @param groupId permission group identifier
     * @param authenticatedEmail authenticated requester email
     * @param request update payload
     * @return updated permission group entity
     */
    @Transactional
    public @NonNull OrganizationPermissionGroupEntity updateGroup(
            @NonNull UUID organizationId,
            @NonNull UUID groupId,
            @NonNull String authenticatedEmail,
            @NonNull UpdatePermissionGroupRequest request
    ) {
        organizationAuthorizationService.requirePermission(
                organizationId, authenticatedEmail, OrganizationPermission.PERMISSION_GROUPS_MANAGE
        );

        OrganizationPermissionGroupEntity group = getGroupOrThrow(organizationId, groupId);
        String name = request.name().trim();
        organizationPermissionGroupRepository.findByOrganizationIdAndName(organizationId, name)
                .filter(existing -> !existing.getId().equals(groupId))
                .ifPresent(existing -> {
                    throw new PermissionGroupConflictException("Permission group name already exists.");
                });

        group.setName(name);
        group.setPermissions(new HashSet<>(normalizePermissions(request.permissions())));
        return organizationPermissionGroupRepository.save(group);
    }

    /**
     * Deletes one permission group.
     *
     * @param organizationId organization identifier
     * @param groupId permission group identifier
     * @param authenticatedEmail authenticated requester email
     */
    @Transactional
    public void deleteGroup(
            @NonNull UUID organizationId,
            @NonNull UUID groupId,
            @NonNull String authenticatedEmail
    ) {
        organizationAuthorizationService.requirePermission(
                organizationId, authenticatedEmail, OrganizationPermission.PERMISSION_GROUPS_MANAGE
        );
        OrganizationPermissionGroupEntity group = getGroupOrThrow(organizationId, groupId);
        organizationPermissionGroupRepository.delete(group);
    }

    /**
     * Replaces permission group assignments for one member.
     *
     * @param organizationId organization identifier
     * @param userId target member user identifier
     * @param authenticatedEmail authenticated requester email
     * @param request assignment payload
     */
    @Transactional
    public void assignMemberGroups(
            @NonNull UUID organizationId,
            @NonNull UUID userId,
            @NonNull String authenticatedEmail,
            @NonNull AssignMemberPermissionGroupsRequest request
    ) {
        organizationAuthorizationService.requirePermission(
                organizationId, authenticatedEmail, OrganizationPermission.PERMISSION_GROUPS_MANAGE
        );

        boolean isMember = organizationMembershipRepository
                .findByIdOrganizationIdAndIdUserId(organizationId, userId)
                .isPresent();
        if (!isMember) {
            throw new OrganizationMemberNotFoundException("Member not found.");
        }

        Set<UUID> groupIds = request.groupIds();
        for (UUID groupId : groupIds) {
            getGroupOrThrow(organizationId, groupId);
        }

        organizationMemberPermissionGroupRepository.deleteByIdOrganizationIdAndIdUserId(organizationId, userId);
        for (UUID groupId : groupIds) {
            organizationMemberPermissionGroupRepository.save(
                    OrganizationMemberPermissionGroupEntity.createNew(organizationId, userId, groupId)
            );
        }
    }

    private @NonNull OrganizationPermissionGroupEntity getGroupOrThrow(
            @NonNull UUID organizationId,
            @NonNull UUID groupId
    ) {
        return organizationPermissionGroupRepository.findById(groupId)
                .filter(group -> group.getOrganizationId().equals(organizationId))
                .orElseThrow(() -> new PermissionGroupNotFoundException("Permission group not found."));
    }

    private @NonNull Set<OrganizationPermission> normalizePermissions(@NonNull Set<OrganizationPermission> permissions) {
        if (permissions.isEmpty()) {
            throw new IllegalArgumentException("At least one permission is required.");
        }
        return Set.copyOf(permissions);
    }
}
