package de.budgetpilot.finance.backend.organization.controller;

import de.budgetpilot.finance.backend.organization.dto.AssignMemberPermissionGroupsRequest;
import de.budgetpilot.finance.backend.organization.dto.CreatePermissionGroupRequest;
import de.budgetpilot.finance.backend.organization.dto.PermissionGroupResponse;
import de.budgetpilot.finance.backend.organization.dto.UpdatePermissionGroupRequest;
import de.budgetpilot.finance.backend.organization.mapper.PermissionGroupMapper;
import de.budgetpilot.finance.backend.organization.service.PermissionGroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * @author Niklas Petermeier
 * @since 18.06.2026
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/organizations/{organizationId}")
public class PermissionGroupController {
    private final PermissionGroupService permissionGroupService;
    private final PermissionGroupMapper permissionGroupMapper;

    @PostMapping("/permission-groups")
    @ResponseStatus(HttpStatus.CREATED)
    /**
     * Creates a custom permission group.
     *
     * @param organizationId organization identifier
     * @param request creation payload
     * @param jwt authenticated JWT token
     * @return created permission group
     */
    public @NonNull PermissionGroupResponse createGroup(
            @PathVariable @NonNull UUID organizationId,
            @Valid @RequestBody @NonNull CreatePermissionGroupRequest request,
            @AuthenticationPrincipal @NonNull Jwt jwt
    ) {
        return permissionGroupMapper.toPermissionGroupResponse(
                permissionGroupService.createGroup(organizationId, extractEmail(jwt), request)
        );
    }

    @GetMapping("/permission-groups")
    /**
     * Lists custom permission groups.
     *
     * @param organizationId organization identifier
     * @param jwt authenticated JWT token
     * @return permission groups
     */
    public @NonNull List<PermissionGroupResponse> listGroups(
            @PathVariable @NonNull UUID organizationId,
            @AuthenticationPrincipal @NonNull Jwt jwt
    ) {
        return permissionGroupService.listGroups(organizationId, extractEmail(jwt)).stream()
                .map(permissionGroupMapper::toPermissionGroupResponse)
                .toList();
    }

    @PutMapping("/permission-groups/{groupId}")
    /**
     * Updates one permission group.
     *
     * @param organizationId organization identifier
     * @param groupId permission group identifier
     * @param request update payload
     * @param jwt authenticated JWT token
     * @return updated permission group
     */
    public @NonNull PermissionGroupResponse updateGroup(
            @PathVariable @NonNull UUID organizationId,
            @PathVariable @NonNull UUID groupId,
            @Valid @RequestBody @NonNull UpdatePermissionGroupRequest request,
            @AuthenticationPrincipal @NonNull Jwt jwt
    ) {
        return permissionGroupMapper.toPermissionGroupResponse(
                permissionGroupService.updateGroup(organizationId, groupId, extractEmail(jwt), request)
        );
    }

    @DeleteMapping("/permission-groups/{groupId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    /**
     * Deletes one permission group.
     *
     * @param organizationId organization identifier
     * @param groupId permission group identifier
     * @param jwt authenticated JWT token
     */
    public void deleteGroup(
            @PathVariable @NonNull UUID organizationId,
            @PathVariable @NonNull UUID groupId,
            @AuthenticationPrincipal @NonNull Jwt jwt
    ) {
        permissionGroupService.deleteGroup(organizationId, groupId, extractEmail(jwt));
    }

    @PutMapping("/members/{userId}/permission-groups")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    /**
     * Replaces permission group assignments for one member.
     *
     * @param organizationId organization identifier
     * @param userId target member user identifier
     * @param request assignment payload
     * @param jwt authenticated JWT token
     */
    public void assignMemberGroups(
            @PathVariable @NonNull UUID organizationId,
            @PathVariable @NonNull UUID userId,
            @Valid @RequestBody @NonNull AssignMemberPermissionGroupsRequest request,
            @AuthenticationPrincipal @NonNull Jwt jwt
    ) {
        permissionGroupService.assignMemberGroups(organizationId, userId, extractEmail(jwt), request);
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
