@file:Suppress("VulnerableLibrariesLocal")

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

repositories {
    maven("https://repo.spongepowered.org/maven")
}

dependencies {
    compileOnly("public:PlaceholderAPI:2.10.9")
    compileOnly("com.google.guava:guava:17.0")
    compileOnly("org.apache.commons:commons-lang3:3.5")
    compileOnly("ink.ptms.core:v11800:11800:api")
    compileOnly(project(":common"))
    compileOnly(project(":common-legacy-api"))
    compileOnly(project(":module:module-chat"))
    compileOnly(project(":module:module-lang"))
    compileOnly(project(":module:module-nms-util"))
    compileOnly(project(":module:module-configuration"))
    compileOnly(project(":expansion:expansion-javascript"))
    compileOnly("org.tabooproject.reflex:reflex:1.0.19")
    compileOnly("org.tabooproject.reflex:analyser:1.0.19")
    compileOnly("org.apache.commons:commons-jexl3:3.2.1")
    compileOnly("com.mojang:datafixerupper:4.0.26")
}

tasks {
    withType<ShadowJar> {
        archiveClassifier.set("")
        relocate("org.tabooproject", "taboolib.library")
        relocate("org.apache.commons.jexl3", "org.apache.commons.jexl3_3_2_1")
    }
    build {
        dependsOn(shadowJar)
    }
}