import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

dependencies {
    compileOnly("org.tabooproject.reflex:reflex:1.0.19")
    compileOnly("org.tabooproject.reflex:analyser:1.0.19")
    compileOnly("com.google.guava:guava:21.0")
    compileOnly("com.google.code.gson:gson:2.8.7")
    compileOnly("net.md-5:bungeecord-chat:1.17")
    compileOnly("ink.ptms:nms-all:1.0.0")
    compileOnly("ink.ptms.core:v11904:11904-minimize:mapped")
    compileOnly("ink.ptms.core:v11904:11904-minimize:universal")
    compileOnly("com.mojang:brigadier:1.0.18")
    compileOnly(project(":common"))
    compileOnly(project(":module:module-nms"))
    compileOnly(project(":platform:platform-bukkit"))
}

tasks {
    withType<ShadowJar> {
        archiveClassifier.set("")
        relocate("org.tabooproject", "taboolib.library")
    }
    build {
        dependsOn(shadowJar)
    }
}