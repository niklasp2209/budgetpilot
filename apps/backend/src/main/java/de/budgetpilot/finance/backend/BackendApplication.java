package de.budgetpilot.finance.backend;

import org.jspecify.annotations.NonNull;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Niklas Petermeier
 * @since 05.05.2026
 */
@SpringBootApplication
public class BackendApplication {

    static void main(@NonNull String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }
}
