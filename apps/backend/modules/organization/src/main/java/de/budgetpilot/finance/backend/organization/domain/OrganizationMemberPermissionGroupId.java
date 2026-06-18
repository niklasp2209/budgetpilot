package de.budgetpilot.finance.backend.organization.domain;

import lombok.EqualsAndHashCode;
import org.jspecify.annotations.NonNull;

import java.io.Serializable;
import java.util.UUID;

/**
 * @author Niklas Petermeier
 * @since 18.06.2026
 */
@EqualsAndHashCode
public class OrganizationMemberPermissionGroupId implements Serializable {
    private UUID organizationId;
    private UUID userId;
    private UUID groupId;

    protected OrganizationMemberPermissionGroupId() {
    }

    public OrganizationMemberPermissionGroupId(
            @NonNull UUID organizationId,
            @NonNull UUID userId,
            @NonNull UUID groupId
    ) {
        this.organizationId = organizationId;
        this.userId = userId;
        this.groupId = groupId;
    }

    public @NonNull UUID getOrganizationId() {
        return organizationId;
    }

    public @NonNull UUID getUserId() {
        return userId;
    }

    public @NonNull UUID getGroupId() {
        return groupId;
    }
}
