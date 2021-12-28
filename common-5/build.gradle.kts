dependencies {
    implementation("com.google.guava:guava:21.0")
    implementation("org.apache.commons:commons-lang3:3.5")
    compileOnly(project(":common"))
}

tasks {
    withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
        archiveClassifier.set("")
        archiveBaseName.set("${archiveBaseName.get()}-shaded")
        dependencies {
            include(dependency("com.google.guava:guava:21.0"))
            include(dependency("org.apache.commons:commons-lang3:3.5"))
        }
    }
    build {
        dependsOn(shadowJar)
    }
}