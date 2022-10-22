import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

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