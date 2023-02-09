import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

dependencies {
    compileOnly("com.google.code.gson:gson:2.8.7")
    compileOnly("net.md-5:bungeecord-chat:1.17")
    compileOnly(project(":common"))
}

tasks {
    withType<ShadowJar> {
        archiveClassifier.set("")
        relocate("net.md_5.bungee", "net.md_5.bungee117")
    }
    build {
        dependsOn(shadowJar)
    }
}