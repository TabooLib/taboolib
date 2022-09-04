repositories {
    maven { url = uri("https://nexus.velocitypowered.com/repository/maven-public/") }
    maven { url = uri("https://repo.mcage.cn/repository/velocity-hosted/") } // 防止 velocitypowered repository 炸裂
}

dependencies {
    compileOnly("org.tabooproject.reflex:reflex:1.0.19")
    compileOnly("com.velocitypowered:velocity-api:3.0.1")
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