package de.budgetpilot.finance.backend.invite.mapper;

import de.budgetpilot.finance.backend.invite.domain.OrganizationInvitationEntity;
import de.budgetpilot.finance.backend.invite.dto.InviteResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

/**
 * @author Niklas Petermeier
 * @since 05.05.2026
 */
@Component
public class InviteMapper {
    /**
     * Maps invitation entity to API response.
     *
     * @param entity invitation entity
     * @return mapped response payload
     */
    public @NonNull InviteResponse toResponse(@NonNull OrganizationInvitationEntity entity) {
        return new InviteResponse(
                entity.getId(),
                entity.getOrganizationId(),
                entity.getInvitedEmail(),
                entity.getRole(),
                entity.getStatus(),
                entity.getToken()
        );
    }
}
