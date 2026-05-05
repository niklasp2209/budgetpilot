package de.budgetpilot.finance.backend.auth.service;

import de.budgetpilot.finance.backend.auth.domain.RefreshTokenEntity;
import de.budgetpilot.finance.backend.auth.exception.InvalidTokenException;
import de.budgetpilot.finance.backend.auth.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.HexFormat;
import java.util.UUID;

/**
 * @author Niklas Petermeier
 * @since 05.05.2026
 */
@Component
@RequiredArgsConstructor
public class RefreshTokenStore {
    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * Stores a newly issued refresh token for a user.
     *
     * @param userId user identifier
     * @param refreshToken raw refresh token
     * @param expiresAt refresh token expiry
     */
    @Transactional
    public void storeNew(@NonNull UUID userId, @NonNull String refreshToken, @NonNull Instant expiresAt) {
        String tokenHash = hash(refreshToken);
        OffsetDateTime expiry = OffsetDateTime.ofInstant(expiresAt, ZoneOffset.UTC);
        refreshTokenRepository.save(RefreshTokenEntity.createNew(userId, tokenHash, expiry));
    }

    /**
     * Rotates a refresh token by revoking the old token and storing a new one.
     *
     * @param userId user identifier
     * @param oldRefreshToken old refresh token
     * @param newRefreshToken new refresh token
     * @param newExpiresAt new refresh token expiry
     */
    @Transactional
    public void rotate(
            @NonNull UUID userId,
            @NonNull String oldRefreshToken,
            @NonNull String newRefreshToken,
            @NonNull Instant newExpiresAt
    ) {
        String oldHash = hash(oldRefreshToken);
        RefreshTokenEntity currentToken = refreshTokenRepository.findByTokenHash(oldHash)
                .orElseThrow(() -> new InvalidTokenException("Refresh token is invalid."));

        if (!currentToken.getUserId().equals(userId)) {
            throw new InvalidTokenException("Refresh token is invalid.");
        }
        if (currentToken.isRevoked()) {
            throw new InvalidTokenException("Refresh token is invalid.");
        }
        if (currentToken.getExpiresAt().isBefore(OffsetDateTime.now(ZoneOffset.UTC))) {
            throw new InvalidTokenException("Refresh token is invalid.");
        }

        currentToken.setRevokedAt(OffsetDateTime.now(ZoneOffset.UTC));
        refreshTokenRepository.save(currentToken);
        storeNew(userId, newRefreshToken, newExpiresAt);
    }

    @Transactional
    public void clear() {
        refreshTokenRepository.deleteAll();
    }

    private @NonNull String hash(@NonNull String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("Missing SHA-256 algorithm.");
        }
    }
}
