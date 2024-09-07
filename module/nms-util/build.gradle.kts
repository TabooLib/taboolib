import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

dependencies {
    compileOnly(project(":common"))
    compileOnly(project(":common-util"))
    compileOnly(project(":common-legacy-api"))
    compileOnly(project(":common-platform-api"))
    compileOnly(project(":module:minecraft-chat"))
    compileOnly(project(":module:nms"))

//    implementation(project(":module:nms-util-legacy"))
//    implementation(project(":module:nms-util-stable"))
//    implementation(project(":module:nms-util-unstable"))
//    implementation(project(":module:nms-util-tag"))
//    implementation(project(":module:nms-util-tag-12005"))
//    implementation(project(":module:nms-util-tag-legacy"))

    // 服务端
    compileOnly("ink.ptms.core:v12100:12100:mapped")
    compileOnly("ink.ptms.core:v12004:12004:mapped")
    compileOnly("ink.ptms.core:v11604:11604")
    // Mojang
    compileOnly("com.mojang:brigadier:1.0.18")
    // compileOnly("com.mojang:authlib:5.0.51")
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