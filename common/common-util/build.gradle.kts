dependencies {
    compileOnly(project(":common:common-core"))
    compileOnly(project(":common:common-environment"))
    // Mirror
    compileOnly(project(":common:common-plugin"))
    compileOnly(project(":common:common-adapter"))
    implementation("com.google.guava:guava:21.0")
    implementation("org.apache.commons:commons-lang3:3.5")
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