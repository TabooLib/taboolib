dependencies {
    compileOnly("com.google.code.gson:gson:2.8.7")
    implementation("net.md-5:bungeecord-chat:1.17")
    compileOnly(project(":common"))
}

tasks {
    withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
        archiveClassifier.set("")
        archiveBaseName.set("${archiveBaseName.get()}-shaded")
        dependencies {
            include(dependency("net.md-5:bungeecord-chat:1.17"))
        }
    }
    build {
        dependsOn(shadowJar)
    }
}