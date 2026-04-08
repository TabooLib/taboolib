import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

dependencies {
    compileOnly(project(":common"))
    compileOnly(project(":common-util"))
    compileOnly(project(":common-platform-api"))
    // ItemRaw
    compileOnly(project(":module:minecraft:minecraft-chat"))
    // Metadata
    compileOnly(project(":module:bukkit:bukkit-util"))
    // 测试用
    compileOnly(project(":platform:platform-bukkit-impl"))
    compileOnly(project(":platform:platform-bukkit"))
    compileOnly(project(":module:bukkit-nms"))
    // 服务端
    compileOnly("net.md-5:bungeecord-chat:1.17")
    compileOnly("ink.ptms.core:v12104:12104:mapped")
    compileOnly("ink.ptms.core:v12005:12005:mapped")
    compileOnly("ink.ptms.core:v12002:12002:mapped")
    compileOnly("ink.ptms.core:v11604:11604")
    compileOnly("ink.ptms.core:v260100:260100-minimize")
    compileOnly("ink.ptms:nms-all:1.0.0")
    // Mojang
    compileOnly("com.mojang:brigadier:1.0.18")
    // DataSerializer
    compileOnly("io.netty:netty-all:4.1.73.Final")
    compileOnly("io.paper:folia-api:1.20.4")
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