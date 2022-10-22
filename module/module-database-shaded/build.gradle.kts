import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

dependencies {
    implementation("com.zaxxer:HikariCP:4.0.3")
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