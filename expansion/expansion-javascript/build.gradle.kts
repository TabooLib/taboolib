dependencies {
    implementation("org.openjdk.nashorn:nashorn-core:15.3")
    compileOnly(project(":common"))
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