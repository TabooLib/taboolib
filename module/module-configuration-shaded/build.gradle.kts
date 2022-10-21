import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

dependencies {
    implementation("org.yaml:snakeyaml:1.32")
    implementation("com.typesafe:config:1.4.2")
    implementation("com.electronwill.night-config:core:3.6.6")
    implementation("com.electronwill.night-config:toml:3.6.6")
    implementation("com.electronwill.night-config:json:3.6.6")
    implementation("com.electronwill.night-config:hocon:3.6.6")
    implementation(project(":module:module-configuration"))
}

tasks {
    withType<ShadowJar> {
        archiveClassifier.set("")
        relocate("org.yaml.snakeyaml.", "org.yaml.snakeyaml_1_32.")
    }
    build {
        dependsOn(shadowJar)
    }
}