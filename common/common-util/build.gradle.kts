dependencies {
    implementation("com.google.code.gson:gson:2.8.9")
    implementation("com.google.guava:guava:21.0")
    implementation("org.apache.commons:commons-lang3:3.5")
    // Core
    compileOnly(project(":common:common-core"))
    compileOnly(project(":common:common-environment"))
    // Mirror
    compileOnly(project(":common:common-plugin"))
    compileOnly(project(":common:common-adapter"))
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