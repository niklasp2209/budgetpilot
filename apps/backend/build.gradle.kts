plugins {
    id("java-library")
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
}
