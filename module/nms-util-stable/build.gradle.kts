import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

dependencies {
    compileOnly(project(":common"))
    compileOnly(project(":common-util"))
    compileOnly(project(":common-platform-api"))
    // ItemRaw
    compileOnly(project(":module:minecraft-chat"))
    compileOnly(project(":module:nms"))
    // 服务端
    compileOnly("net.md-5:bungeecord-chat:1.17")
    compileOnly("ink.ptms.core:v12100:12100:mapped")
    compileOnly("ink.ptms.core:v12100:12100:universal")
    compileOnly("ink.ptms.core:v11604:11604")
    compileOnly("ink.ptms:nms-all:1.0.0")
    // Mojang
    compileOnly("com.mojang:brigadier:1.0.18")
    // DataSerializer
    compileOnly("io.netty:netty-all:4.1.73.Final")
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