plugins {
    id("org.springframework.boot") version "4.0.6"
    id("io.spring.dependency-management") version "1.1.7"
}

dependencies {
    add("implementation", project(":apps:backend:modules:auth"))
    add("implementation", project(":apps:backend:modules:organization"))
    add("implementation", project(":apps:backend:modules:membership"))
    add("implementation", project(":apps:backend:modules:invite"))
    add("implementation", project(":apps:backend:modules:accounting"))
    add("implementation", project(":apps:backend:modules:budget"))
    add("implementation", project(":apps:backend:modules:reporting"))
    add("implementation", project(":apps:backend:modules:audit"))
    add("implementation", project(":apps:backend:modules:shared"))
    add("implementation", "org.springframework.boot:spring-boot-starter-data-jpa")
    add("implementation", "org.springframework.boot:spring-boot-starter-webmvc")
    add("implementation", "org.springframework.boot:spring-boot-starter-security")
    add("implementation", "org.springframework.boot:spring-boot-starter-oauth2-resource-server")
    add("implementation", "org.springframework.boot:spring-boot-starter-actuator")
    add("implementation", "org.flywaydb:flyway-core")
    add("implementation", "org.flywaydb:flyway-database-postgresql")
    add("runtimeOnly", "org.postgresql:postgresql")
    add("testImplementation", "org.springframework.boot:spring-boot-starter-json")
    add("testImplementation", "org.springframework.boot:spring-boot-starter-test")
    add("testImplementation", "org.springframework.boot:spring-boot-testcontainers")
    add("testImplementation", "org.springframework.boot:spring-boot-starter-webmvc-test")
    add("testImplementation", "org.springframework.security:spring-security-test")
    add("testImplementation", platform("org.testcontainers:testcontainers-bom:2.0.5"))
    add("testImplementation", "org.testcontainers:testcontainers")
    add("testImplementation", "org.testcontainers:testcontainers-junit-jupiter")
}
