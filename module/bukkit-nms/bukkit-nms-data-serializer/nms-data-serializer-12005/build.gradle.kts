import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

dependencies {
    compileOnly(project(":common-util"))
    compileOnly(project(":module:bukkit-nms"))
    compileOnly(project(":module:bukkit-nms:bukkit-nms-data-serializer"))
    // 服务端
    compileOnly("ink.ptms.core:v260100:260100-minimize")
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