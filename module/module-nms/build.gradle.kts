import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

dependencies {
    compileOnly("org.tabooproject.reflex:reflex:1.0.19")
    compileOnly("org.tabooproject.reflex:analyser:1.0.19")
    compileOnly("com.google.code.gson:gson:2.8.7")
    compileOnly("com.google.guava:guava:21.0")
    compileOnly("org.ow2.asm:asm:9.1")
    compileOnly("org.ow2.asm:asm-commons:9.1")
    compileOnly("io.netty:netty-all:5.0.0.Alpha2")
    compileOnly("ink.ptms:nms-all:1.0.0")
    compileOnly("ink.ptms.core:v12000:12000-minimize:mapped")
    compileOnly("ink.ptms.core:v11904:11904-minimize:mapped")
    compileOnly("ink.ptms.core:v11800:11800-minimize:mapped")
    compileOnly("ink.ptms.core:v11200:11200-minimize")
    compileOnly(project(":common"))
    compileOnly(project(":platform:platform-bukkit"))
}

tasks {
    withType<ShadowJar> {
        archiveClassifier.set("")
        relocate("me.lucko", "taboolib.library")
        relocate("org.objectweb", "taboolib.library")
        relocate("org.tabooproject", "taboolib.library")
    }
    build {
        dependsOn(shadowJar)
    }
}