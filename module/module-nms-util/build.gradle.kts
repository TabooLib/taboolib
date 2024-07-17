import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

dependencies {
    compileOnly(project(":common"))
    compileOnly(project(":common-util"))
    compileOnly(project(":common-legacy-api"))
    compileOnly(project(":common-platform-api"))
    compileOnly(project(":module:module-chat"))
    compileOnly(project(":module:module-nms"))
    implementation(project(":module:module-nms-util-legacy"))
    implementation(project(":module:module-nms-util-stable"))
    implementation(project(":module:module-nms-util-unstable"))
    implementation(project(":module:module-nms-util-tag"))
    implementation(project(":module:module-nms-util-tag-12005"))
    implementation(project(":module:module-nms-util-tag-legacy"))
    compileOnly(project(":module:module-bukkit-util"))
    compileOnly(project(":module:module-bukkit-xseries"))
    compileOnly(project(":platform:platform-bukkit"))
    // 服务端
    compileOnly("net.md-5:bungeecord-chat:1.17")
    compileOnly("ink.ptms:nms-all:1.0.0")
    compileOnly("ink.ptms.core:v12002:12002-minimize:mapped")
    compileOnly("ink.ptms.core:v12002:12002-minimize:universal")
    compileOnly("ink.ptms.core:v12004:12004-minimize:universal")
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