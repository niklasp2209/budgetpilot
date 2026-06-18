plugins {
    id("java-library")
}

dependencies {
    add("implementation", platform("org.springframework.boot:spring-boot-dependencies:4.0.6"))
    add("implementation", project(":apps:backend:modules:shared"))
    add("implementation", project(":apps:backend:modules:auth"))
    add("implementation", project(":apps:backend:modules:organization"))
    add("implementation", project(":apps:backend:modules:accounting"))
    add("implementation", project(":apps:backend:modules:budget"))
    add("implementation", "org.springframework.boot:spring-boot-starter-data-jpa")
    add("implementation", "org.springframework.boot:spring-boot-starter-webmvc")
    add("implementation", "org.springframework.boot:spring-boot-starter-security")
    add("implementation", "org.springframework.boot:spring-boot-starter-oauth2-resource-server")
}
