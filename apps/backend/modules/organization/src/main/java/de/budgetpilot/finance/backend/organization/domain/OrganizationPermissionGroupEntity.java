package de.budgetpilot.finance.backend.organization.domain;

import de.budgetpilot.finance.backend.organization.authorization.OrganizationPermission;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jspecify.annotations.NonNull;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * @author Niklas Petermeier
 * @since 18.06.2026
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "organization_permission_groups")
public class OrganizationPermissionGroupEntity {
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "organization_id", nullable = false, updatable = false)
    private UUID organizationId;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "organization_permission_group_permissions",
            joinColumns = @JoinColumn(name = "group_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "permission", nullable = false, length = 64)
    private Set<OrganizationPermission> permissions = new HashSet<>();

    public static @NonNull OrganizationPermissionGroupEntity createNew(
            @NonNull UUID organizationId,
            @NonNull String name,
            @NonNull Set<OrganizationPermission> permissions
    ) {
        OrganizationPermissionGroupEntity entity = new OrganizationPermissionGroupEntity();
        entity.setId(UUID.randomUUID());
        entity.setOrganizationId(organizationId);
        entity.setName(name);
        entity.setCreatedAt(OffsetDateTime.now());
        entity.setPermissions(new HashSet<>(permissions));
        return entity;
    }
}
