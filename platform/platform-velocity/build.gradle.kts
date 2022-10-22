import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
repositories {
    maven { url = uri("https://repo.papermc.io/repository/maven-public/") }
}

dependencies {
    compileOnly("org.tabooproject.reflex:reflex:1.0.19")
    compileOnly("com.velocitypowered:velocity-api:3.1.1")
    compileOnly(project(":common"))
}

tasks {
    withType<ShadowJar> {
        archiveClassifier.set("")
        relocate("org.tabooproject", "taboolib.library")
    }
    build {
        dependsOn(shadowJar)
    }
}