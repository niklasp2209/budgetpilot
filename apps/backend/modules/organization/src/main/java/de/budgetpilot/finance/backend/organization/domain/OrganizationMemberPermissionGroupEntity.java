package de.budgetpilot.finance.backend.organization.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jspecify.annotations.NonNull;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * @author Niklas Petermeier
 * @since 18.06.2026
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "organization_member_permission_groups")
public class OrganizationMemberPermissionGroupEntity {
    @EmbeddedId
    private OrganizationMemberPermissionGroupId id;

    @Column(name = "assigned_at", nullable = false, updatable = false)
    private OffsetDateTime assignedAt;

    public static @NonNull OrganizationMemberPermissionGroupEntity createNew(
            @NonNull UUID organizationId,
            @NonNull UUID userId,
            @NonNull UUID groupId
    ) {
        OrganizationMemberPermissionGroupEntity entity = new OrganizationMemberPermissionGroupEntity();
        entity.setId(new OrganizationMemberPermissionGroupId(organizationId, userId, groupId));
        entity.setAssignedAt(OffsetDateTime.now());
        return entity;
    }
}
