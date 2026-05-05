package de.budgetpilot.finance.backend.invite.dto;

import de.budgetpilot.finance.backend.organization.domain.MembershipRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.NonNull;

/**
 * @author Niklas Petermeier
 * @since 05.05.2026
 */
public record CreateInviteRequest(
        @NonNull @NotBlank @Email String email,
        @NonNull @NotNull MembershipRole role
) {
}
