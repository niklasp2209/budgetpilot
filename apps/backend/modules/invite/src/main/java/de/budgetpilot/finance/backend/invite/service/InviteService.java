package de.budgetpilot.finance.backend.invite.service;

import de.budgetpilot.finance.backend.auth.domain.AuthUserEntity;
import de.budgetpilot.finance.backend.auth.repository.AuthUserRepository;
import de.budgetpilot.finance.backend.invite.domain.InvitationStatus;
import de.budgetpilot.finance.backend.invite.domain.OrganizationInvitationEntity;
import de.budgetpilot.finance.backend.invite.dto.CreateInviteRequest;
import de.budgetpilot.finance.backend.invite.exception.InviteAccessDeniedException;
import de.budgetpilot.finance.backend.invite.exception.InviteInvalidException;
import de.budgetpilot.finance.backend.invite.repository.OrganizationInvitationRepository;
import de.budgetpilot.finance.backend.organization.domain.MembershipRole;
import de.budgetpilot.finance.backend.organization.domain.OrganizationMembershipEntity;
import de.budgetpilot.finance.backend.organization.repository.OrganizationMembershipRepository;
import de.budgetpilot.finance.backend.organization.repository.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * @author Niklas Petermeier
 * @since 05.05.2026
 */
@Service
@RequiredArgsConstructor
public class InviteService {
    private final OrganizationInvitationRepository invitationRepository;
    private final OrganizationMembershipRepository membershipRepository;
    private final OrganizationRepository organizationRepository;
    private final AuthUserRepository authUserRepository;

    /**
     * Creates a new invitation for an organization.
     *
     * @param organizationId organization identifier
     * @param requesterEmail authenticated requester email
     * @param request invite creation payload
     * @return created invitation entity
     */
    @Transactional
    public @NonNull OrganizationInvitationEntity createInvite(
            @NonNull UUID organizationId,
            @NonNull String requesterEmail,
            @NonNull CreateInviteRequest request
    ) {
        AuthUserEntity requester = findUserByEmail(requesterEmail);
        OrganizationMembershipEntity requesterMembership = findMembership(organizationId, requester.getId());
        MembershipRole requesterRole = requesterMembership.getRole();
        if (requesterRole != MembershipRole.OWNER && requesterRole != MembershipRole.ADMIN) {
            throw new InviteAccessDeniedException("Only OWNER or ADMIN can create invites.");
        }
        if (request.role() == MembershipRole.OWNER) {
            throw new InviteAccessDeniedException("Invites cannot assign OWNER role.");
        }
        if (!organizationRepository.existsById(organizationId)) {
            throw new InviteInvalidException("Organization not found.");
        }

        String invitedEmail = normalizeEmail(request.email());
        String token = UUID.randomUUID().toString();
        OrganizationInvitationEntity entity = OrganizationInvitationEntity.createNew(
                organizationId,
                invitedEmail,
                request.role(),
                token,
                requester.getId(),
                OffsetDateTime.now().plusDays(7)
        );
        return invitationRepository.save(entity);
    }

    /**
     * Returns active invitations for an organization.
     *
     * @param organizationId organization identifier
     * @param requesterEmail authenticated requester email
     * @return active invitation entities
     */
    @Transactional(readOnly = true)
    public @NonNull List<OrganizationInvitationEntity> listInvites(
            @NonNull UUID organizationId,
            @NonNull String requesterEmail
    ) {
        AuthUserEntity requester = findUserByEmail(requesterEmail);
        OrganizationMembershipEntity requesterMembership = findMembership(organizationId, requester.getId());
        MembershipRole requesterRole = requesterMembership.getRole();
        if (requesterRole != MembershipRole.OWNER && requesterRole != MembershipRole.ADMIN) {
            throw new InviteAccessDeniedException("Only OWNER or ADMIN can list invites.");
        }
        return invitationRepository.findByOrganizationId(organizationId).stream()
                .filter(entity -> entity.getStatus() == InvitationStatus.PENDING)
                .toList();
    }

    /**
     * Accepts an invitation token for the authenticated user.
     *
     * @param token invitation token
     * @param requesterEmail authenticated requester email
     */
    @Transactional
    public void acceptInvite(@NonNull String token, @NonNull String requesterEmail) {
        AuthUserEntity requester = findUserByEmail(requesterEmail);
        OrganizationInvitationEntity invitation = findActiveInvitation(token);
        String normalizedRequesterEmail = normalizeEmail(requesterEmail);
        if (!invitation.getInvitedEmail().equals(normalizedRequesterEmail)) {
            throw new InviteAccessDeniedException("Invite is not for authenticated user.");
        }
        if (membershipRepository.findByIdOrganizationIdAndIdUserId(invitation.getOrganizationId(), requester.getId()).isPresent()) {
            throw new InviteInvalidException("User is already a member.");
        }

        membershipRepository.save(OrganizationMembershipEntity.createNew(
                invitation.getOrganizationId(),
                requester.getId(),
                invitation.getRole()
        ));
        invitation.setStatus(InvitationStatus.ACCEPTED);
        invitation.setAcceptedAt(OffsetDateTime.now());
        invitationRepository.save(invitation);
    }

    /**
     * Declines an invitation token for the authenticated user.
     *
     * @param token invitation token
     * @param requesterEmail authenticated requester email
     */
    @Transactional
    public void declineInvite(@NonNull String token, @NonNull String requesterEmail) {
        OrganizationInvitationEntity invitation = findActiveInvitation(token);
        String normalizedRequesterEmail = normalizeEmail(requesterEmail);
        if (!invitation.getInvitedEmail().equals(normalizedRequesterEmail)) {
            throw new InviteAccessDeniedException("Invite is not for authenticated user.");
        }
        invitation.setStatus(InvitationStatus.DECLINED);
        invitation.setDeclinedAt(OffsetDateTime.now());
        invitationRepository.save(invitation);
    }

    /**
     * Resolves and validates an active invitation token.
     *
     * @param token invitation token
     * @return active invitation
     */
    private @NonNull OrganizationInvitationEntity findActiveInvitation(@NonNull String token) {
        OrganizationInvitationEntity invitation = invitationRepository.findByToken(token)
                .orElseThrow(() -> new InviteInvalidException("Invite token is invalid."));
        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new InviteInvalidException("Invite is no longer active.");
        }
        if (invitation.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new InviteInvalidException("Invite is expired.");
        }
        return invitation;
    }

    /**
     * Resolves organization membership for a user.
     *
     * @param organizationId organization identifier
     * @param userId user identifier
     * @return matching membership entity
     */
    private @NonNull OrganizationMembershipEntity findMembership(@NonNull UUID organizationId, @NonNull UUID userId) {
        return membershipRepository.findByIdOrganizationIdAndIdUserId(organizationId, userId)
                .orElseThrow(() -> new InviteAccessDeniedException("Organization access denied."));
    }

    /**
     * Resolves an authenticated user by email.
     *
     * @param email user email address
     * @return resolved auth user entity
     */
    private @NonNull AuthUserEntity findUserByEmail(@NonNull String email) {
        return authUserRepository.findByEmail(normalizeEmail(email))
                .orElseThrow(() -> new InviteAccessDeniedException("Authenticated user was not found."));
    }

    /**
     * Normalizes email values for lookup and comparison.
     *
     * @param email raw email
     * @return normalized email
     */
    private @NonNull String normalizeEmail(@NonNull String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
