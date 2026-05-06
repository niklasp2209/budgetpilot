package de.budgetpilot.finance.backend.auth;

import org.flywaydb.core.Flyway;
import org.jspecify.annotations.NonNull;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;

/**
 * @author Niklas Petermeier
 * @since 05.05.2026
 */
public abstract class AbstractPostgresIntegrationTest {
    @SuppressWarnings("resource")
    private static final GenericContainer<?> POSTGRES =
            new GenericContainer<>("postgres:16-alpine")
                    .withExposedPorts(5432)
                    .withEnv("POSTGRES_DB", "budgetpilot_test")
                    .withEnv("POSTGRES_USER", "budgetpilot")
                    .withEnv("POSTGRES_PASSWORD", "budgetpilot");

    @DynamicPropertySource
    static void configureDatasource(@NonNull DynamicPropertyRegistry registry) {
        registry.add(
                "spring.datasource.url",
                () -> "jdbc:postgresql://%s:%d/budgetpilot_test".formatted(
                        POSTGRES.getHost(),
                        POSTGRES.getMappedPort(5432)
                )
        );
        registry.add("spring.datasource.username", () -> "budgetpilot");
        registry.add("spring.datasource.password", () -> "budgetpilot");
        registry.add("spring.flyway.enabled", () -> "false");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");

        migrateOnce();
    }

    private static boolean migrated = false;

    private static synchronized void migrateOnce() {
        if (migrated) {
            return;
        }
        String jdbcUrl = "jdbc:postgresql://%s:%d/budgetpilot_test".formatted(
                POSTGRES.getHost(),
                POSTGRES.getMappedPort(5432)
        );
        Flyway.configure()
                .dataSource(jdbcUrl, "budgetpilot", "budgetpilot")
                .locations("classpath:db/migration")
                .load()
                .migrate();
        migrated = true;
    }

    static {
        POSTGRES.start();
    }
}
