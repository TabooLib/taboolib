dependencies {
    compileOnly("com.zaxxer:HikariCP:4.0.3")
    compileOnly(project(":common"))
    compileOnly(project(":module:module-configuration"))
}

tasks {
    withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
        archiveClassifier.set("")
        relocate("com.zaxxer.hikari", "com.zaxxer.hikari_4_0_3")
    }
    build {
        dependsOn(shadowJar)
    }
}