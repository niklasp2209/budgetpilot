package de.budgetpilot.finance.backend.organization.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
@Table(name = "organizations")
public class OrganizationEntity {
    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "slug", nullable = false, unique = true, length = 255)
    private String slug;

    @Column(name = "created_by", nullable = false, updatable = false)
    private UUID createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    public static @NonNull OrganizationEntity createNew(
            @NonNull String name,
            @NonNull String slug,
            @NonNull UUID createdBy,
            @NonNull String currency
    ) {
        OrganizationEntity entity = new OrganizationEntity();
        entity.setId(UUID.randomUUID());
        entity.setName(name);
        entity.setSlug(slug);
        entity.setCreatedBy(createdBy);
        entity.setCreatedAt(OffsetDateTime.now());
        entity.setCurrency(currency);
        return entity;
    }
}
