package de.budgetpilot.finance.backend.organization.dto;

import de.budgetpilot.finance.backend.organization.domain.MembershipRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * @author Niklas Petermeier
 * @since 18.06.2026
 */
public record AddOrganizationMemberRequest(
        @NonNull @NotBlank @Email String email,
        @Nullable @Size(min = 8, max = 128) String password,
        @NonNull @NotNull MembershipRole role
) {
}
