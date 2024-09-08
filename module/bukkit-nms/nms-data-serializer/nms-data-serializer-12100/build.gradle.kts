import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

dependencies {
    compileOnly(project(":common-util"))
    compileOnly(project(":module:bukkit-nms"))
    compileOnly(project(":module:bukkit-nms:nms-data-serializer"))
    // 服务端
    compileOnly("ink.ptms.core:v12101:12101:mapped")
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