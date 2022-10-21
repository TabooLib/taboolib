import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

dependencies {
    implementation(project(":module:module-database-core"))
}

tasks {
    withType<ShadowJar> {
        archiveClassifier.set("")
        relocate("com.zaxxer.hikari.", "com.zaxxer.hikari_4_0_3.")
    }
    build {
        dependsOn(shadowJar)
    }
}