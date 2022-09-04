dependencies {
    compileOnly("org.tabooproject.reflex:reflex:1.0.19")
    compileOnly("net.md_5.bungee:BungeeCord:1")
    compileOnly(project(":common"))
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