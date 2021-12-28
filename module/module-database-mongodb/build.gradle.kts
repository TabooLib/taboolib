dependencies {
    implementation("com.mongodb:MongoDB:3.12.2")
    implementation("com.google.code.gson:gson:2.8.7")
    compileOnly(project(":common"))
    compileOnly(project(":module:module-configuration"))
}

tasks {
    withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
        archiveClassifier.set("")
        archiveBaseName.set("${archiveBaseName.get()}-shaded")
        dependencies {
            include(dependency("com.mongodb:MongoDB:3.12.2"))
            include(dependency("com.google.code.gson:gson:2.8.7"))
        }
    }
    build {
        dependsOn(shadowJar)
    }
}