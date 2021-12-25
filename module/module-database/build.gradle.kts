dependencies {
    implementation("com.zaxxer:HikariCP:4.0.3")
    compileOnly(project(":common"))
    compileOnly(project(":module:module-configuration"))
}

tasks {
    withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
        archiveClassifier.set("")
        archiveBaseName.set("${archiveBaseName.get()}-shaded")
    }
    build {
        dependsOn(shadowJar)
    }
}