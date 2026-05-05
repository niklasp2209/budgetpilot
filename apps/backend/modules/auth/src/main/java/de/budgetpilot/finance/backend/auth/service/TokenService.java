package de.budgetpilot.finance.backend.auth.service;

import de.budgetpilot.finance.backend.auth.config.JwtProperties;
import de.budgetpilot.finance.backend.auth.domain.TokenPair;
import de.budgetpilot.finance.backend.auth.exception.InvalidTokenException;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * @author Niklas Petermeier
 * @since 05.05.2026
 */
@Service
@RequiredArgsConstructor
public class TokenService {
    private static final String TOKEN_TYPE_CLAIM = "token_type";
    private static final String ACCESS_TOKEN_TYPE = "access";
    private static final String REFRESH_TOKEN_TYPE = "refresh";

    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final JwtProperties jwtProperties;

    /**
     * Issues a new access and refresh token pair for a subject.
     *
     * @param email token subject
     * @return token pair with access expiration metadata
     */
    public @NonNull TokenPair issueTokenPair(@NonNull String email) {
        Duration accessTtl = Duration.ofSeconds(jwtProperties.accessTtlSeconds());
        Duration refreshTtl = Duration.ofSeconds(jwtProperties.refreshTtlSeconds());
        String accessToken = createToken(email, ACCESS_TOKEN_TYPE, accessTtl);
        String refreshToken = createToken(email, REFRESH_TOKEN_TYPE, refreshTtl);
        return new TokenPair(accessToken, refreshToken, accessTtl.toSeconds());
    }

    /**
     * Validates a refresh token and returns its subject.
     *
     * @param refreshToken refresh token to validate
     * @return the subject from the valid refresh token
     */
    public @NonNull String validateAndExtractRefreshSubject(@NonNull String refreshToken) {
        Jwt jwt = validateAndDecodeRefreshToken(refreshToken);
        return Optional.ofNullable(jwt.getSubject())
                .filter(value -> !value.isBlank())
                .orElseThrow(() -> new InvalidTokenException("Refresh token is invalid."));
    }

    public @NonNull Instant validateAndExtractRefreshExpiration(@NonNull String refreshToken) {
        Jwt jwt = validateAndDecodeRefreshToken(refreshToken);
        Instant expiresAt = jwt.getExpiresAt();
        if (expiresAt == null) {
            throw new InvalidTokenException("Refresh token is invalid.");
        }
        return expiresAt;
    }

    private @NonNull Jwt validateAndDecodeRefreshToken(@NonNull String refreshToken) {
        Jwt jwt = decode(refreshToken);
        String tokenType = Optional.ofNullable(jwt.getClaimAsString(TOKEN_TYPE_CLAIM)).orElse("");
        if (!REFRESH_TOKEN_TYPE.equals(tokenType)) {
            throw new InvalidTokenException("Refresh token is invalid.");
        }
        return jwt;
    }

    private String createToken(@NonNull String subject, @NonNull String tokenType, @NonNull Duration ttl) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .id(UUID.randomUUID().toString())
                .subject(subject)
                .issuedAt(now)
                .expiresAt(now.plus(ttl))
                .claim(TOKEN_TYPE_CLAIM, tokenType)
                .build();

        JwsHeader jwsHeader = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();
    }

    private Jwt decode(@NonNull String token) {
        try {
            return jwtDecoder.decode(token);
        } catch (JwtException exception) {
            throw new InvalidTokenException("Refresh token is invalid.");
        }
    }
}
