package de.budgetpilot.finance.backend.organization.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

/**
 * @author Niklas Petermeier
 * @since 05.05.2026
 */
@Getter
@Setter
@NoArgsConstructor
@Embeddable
public class OrganizationMembershipId implements Serializable {
    @Column(name = "organization_id", nullable = false, updatable = false)
    private UUID organizationId;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    public OrganizationMembershipId(UUID organizationId, UUID userId) {
        this.organizationId = organizationId;
        this.userId = userId;
    }
}
