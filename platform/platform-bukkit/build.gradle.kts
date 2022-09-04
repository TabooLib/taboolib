@file:Suppress("GradlePackageUpdate", "VulnerableLibrariesLocal")

repositories {
    maven { url = uri("https://jitpack.io") }
    maven { url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/" )}
}

dependencies {
    compileOnly("public:PlaceholderAPI:2.10.9")
    compileOnly("net.kyori:adventure-api:4.9.2")
    compileOnly("net.milkbowl.vault:Vault:1")
    compileOnly("com.google.guava:guava:21.0")
    compileOnly("com.google.code.gson:gson:2.8.7")
    compileOnly("org.apache.commons:commons-lang3:3.5")
    compileOnly("ink.ptms.core:v11701:11701-minimize:universal")
    compileOnly("ink.ptms.core:v11600:11600-minimize")
    compileOnly("ink.ptms.core:v11200:11200-minimize")
    compileOnly("ink.ptms.core:v11400:11400-minimize")
    compileOnly(project(":common"))
    compileOnly(project(":common-5"))
    compileOnly(project(":module:module-chat"))
    compileOnly(project(":module:module-lang"))
    compileOnly(project(":module:module-configuration"))
    compileOnly("org.tabooproject.reflex:reflex:1.0.19")
    compileOnly("org.tabooproject.reflex:analyser:1.0.19")
}

tasks {
    withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
        archiveClassifier.set("")
        relocate("org.tabooproject", "taboolib.library")
    }
    build {
        dependsOn(shadowJar)
    }
}