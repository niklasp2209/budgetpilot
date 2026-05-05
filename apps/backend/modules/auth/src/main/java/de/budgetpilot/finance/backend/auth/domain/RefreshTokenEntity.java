package de.budgetpilot.finance.backend.auth.domain;

import jakarta.persistence.*;
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
@Table(name = "refresh_tokens")
public class RefreshTokenEntity {
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "token_hash", nullable = false, unique = true, length = 128)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "revoked_at")
    private @Nullable OffsetDateTime revokedAt;

    public static @NonNull RefreshTokenEntity createNew(
            @NonNull UUID userId,
            @NonNull String tokenHash,
            @NonNull OffsetDateTime expiresAt
    ) {
        RefreshTokenEntity entity = new RefreshTokenEntity();
        entity.setId(UUID.randomUUID());
        entity.setUserId(userId);
        entity.setTokenHash(tokenHash);
        entity.setExpiresAt(expiresAt);
        entity.setCreatedAt(OffsetDateTime.now());
        entity.setRevokedAt(null);
        return entity;
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }
}
