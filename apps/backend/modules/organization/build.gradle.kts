plugins {
    id("java-library")
}

dependencies {
    add("implementation", project(":apps:backend:modules:shared"))
}
