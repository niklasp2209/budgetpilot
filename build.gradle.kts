import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.jvm.toolchain.JavaLanguageVersion

plugins {
    base
}

group = "de.budgetpilot.finance"
version = "1.0.0"

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    if (path.startsWith(":apps:backend")) {
        apply(plugin = "java")

        extensions.configure<JavaPluginExtension> {
            toolchain {
                languageVersion.set(JavaLanguageVersion.of(25))
            }
        }

        dependencies {
            "compileOnly"("org.jspecify:jspecify:1.0.0")
            "testCompileOnly"("org.jspecify:jspecify:1.0.0")
            "compileOnly"("org.projectlombok:lombok:1.18.38")
            "annotationProcessor"("org.projectlombok:lombok:1.18.38")
            "testCompileOnly"("org.projectlombok:lombok:1.18.38")
            "testAnnotationProcessor"("org.projectlombok:lombok:1.18.38")
            "testImplementation"(platform("org.junit:junit-bom:6.0.0"))
            "testImplementation"("org.junit.jupiter:junit-jupiter")
            "testRuntimeOnly"("org.junit.platform:junit-platform-launcher")
        }

        tasks.withType<Test>().configureEach {
            useJUnitPlatform()
        }
    }
}