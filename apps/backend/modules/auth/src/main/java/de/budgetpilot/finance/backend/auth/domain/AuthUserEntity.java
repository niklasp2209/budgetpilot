package de.budgetpilot.finance.backend.auth.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jspecify.annotations.NonNull;

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
@Table(name = "users")
public class AuthUserEntity {
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "email", nullable = false, unique = true, length = 320)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "status", nullable = false, length = 32)
    private String status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    public static @NonNull AuthUserEntity createNew(@NonNull String email, @NonNull String passwordHash) {
        AuthUserEntity entity = new AuthUserEntity();
        entity.setId(UUID.randomUUID());
        entity.setEmail(email);
        entity.setPasswordHash(passwordHash);
        entity.setStatus("ACTIVE");
        entity.setCreatedAt(OffsetDateTime.now());
        return entity;
    }
}
