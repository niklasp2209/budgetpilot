package de.budgetpilot.finance.backend.organization.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jspecify.annotations.NonNull;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * @author Niklas Petermeier
 * @since 05.05.2026
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "organization_memberships")
public class OrganizationMembershipEntity {
    @EmbeddedId
    private OrganizationMembershipId id;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 32)
    private MembershipRole role;

    @Column(name = "joined_at", nullable = false, updatable = false)
    private OffsetDateTime joinedAt;

    @Column(name = "status", nullable = false, length = 32)
    private String status;

    public static @NonNull OrganizationMembershipEntity createNew(
            @NonNull UUID organizationId,
            @NonNull UUID userId,
            @NonNull MembershipRole role
    ) {
        OrganizationMembershipEntity entity = new OrganizationMembershipEntity();
        entity.setId(new OrganizationMembershipId(organizationId, userId));
        entity.setRole(role);
        entity.setJoinedAt(OffsetDateTime.now());
        entity.setStatus("ACTIVE");
        return entity;
    }
}
