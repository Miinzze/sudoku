pluginManagement {
    repositories {
        maven { url = uri("https://maven.google.com") }
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    // Lets Gradle auto-download a matching JDK (e.g. 17) when jvmToolchain(17) is requested but
    // no local installation matches, instead of failing with "toolchain download repositories
    // have not been configured". Without this, every contributor must have a JDK 17 already on
    // PATH or configured via org.gradle.java.installations.* — this makes first-time setup work
    // out of the box.
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven { url = uri("https://maven.google.com") }
        mavenCentral()
    }
}

rootProject.name = "SudokuAI"
include(":app")
include(":core")
