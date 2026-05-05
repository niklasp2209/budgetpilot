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
    add("implementation", "org.springframework.boot:spring-boot-starter-webmvc")
    add("implementation", "org.springframework.boot:spring-boot-starter-actuator")
    add("testImplementation", "org.springframework.boot:spring-boot-starter-json")
    add("testImplementation", "org.springframework.boot:spring-boot-starter-test")
    add("testImplementation", "org.springframework.boot:spring-boot-starter-webmvc-test")
    add("testImplementation", "org.springframework.security:spring-security-test")
}
