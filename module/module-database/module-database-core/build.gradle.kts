dependencies {
    compileOnly("com.zaxxer:HikariCP:4.0.3")
    compileOnly(project(":common:common-core"))
    compileOnly(project(":module:module-configuration"))
}

tasks {
    withType<Jar> {
        destinationDirectory.set(file("build/libs"))
    }
    withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
        archiveClassifier.set("")
        relocate("com.zaxxer.hikari", "com.zaxxer.hikari_4_0_3")
    }
}