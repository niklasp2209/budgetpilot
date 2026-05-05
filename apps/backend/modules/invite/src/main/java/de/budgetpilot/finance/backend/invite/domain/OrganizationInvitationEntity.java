package de.budgetpilot.finance.backend.invite.domain;

import de.budgetpilot.finance.backend.organization.domain.MembershipRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

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
@Table(name = "organization_invitations")
public class OrganizationInvitationEntity {
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "organization_id", nullable = false, updatable = false)
    private UUID organizationId;

    @Column(name = "invited_email", nullable = false, length = 320)
    private String invitedEmail;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 32)
    private MembershipRole role;

    @Column(name = "token", nullable = false, unique = true, length = 128)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private InvitationStatus status;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    @Column(name = "accepted_at")
    private @Nullable OffsetDateTime acceptedAt;

    @Column(name = "declined_at")
    private @Nullable OffsetDateTime declinedAt;

    @Column(name = "created_by", nullable = false, updatable = false)
    private UUID createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    /**
     * Creates a new invitation entity in pending state.
     *
     * @param organizationId target organization identifier
     * @param invitedEmail invited user email
     * @param role role granted after acceptance
     * @param token invitation token
     * @param createdBy creator user identifier
     * @param expiresAt invitation expiration timestamp
     * @return persisted-ready invitation entity
     */
    public static @NonNull OrganizationInvitationEntity createNew(
            @NonNull UUID organizationId,
            @NonNull String invitedEmail,
            @NonNull MembershipRole role,
            @NonNull String token,
            @NonNull UUID createdBy,
            @NonNull OffsetDateTime expiresAt
    ) {
        OrganizationInvitationEntity entity = new OrganizationInvitationEntity();
        entity.setId(UUID.randomUUID());
        entity.setOrganizationId(organizationId);
        entity.setInvitedEmail(invitedEmail);
        entity.setRole(role);
        entity.setToken(token);
        entity.setStatus(InvitationStatus.PENDING);
        entity.setExpiresAt(expiresAt);
        entity.setCreatedBy(createdBy);
        entity.setCreatedAt(OffsetDateTime.now());
        return entity;
    }
}
