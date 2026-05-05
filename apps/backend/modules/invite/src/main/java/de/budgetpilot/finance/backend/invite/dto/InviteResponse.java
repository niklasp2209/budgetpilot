package de.budgetpilot.finance.backend.invite.dto;

import de.budgetpilot.finance.backend.invite.domain.InvitationStatus;
import de.budgetpilot.finance.backend.organization.domain.MembershipRole;
import org.jspecify.annotations.NonNull;

import java.util.UUID;

/**
 * @author Niklas Petermeier
 * @since 05.05.2026
 */
public record InviteResponse(
        @NonNull UUID id,
        @NonNull UUID organizationId,
        @NonNull String invitedEmail,
        @NonNull MembershipRole role,
        @NonNull InvitationStatus status,
        @NonNull String token
) {
}
