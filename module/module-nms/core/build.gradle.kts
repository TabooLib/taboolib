import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

dependencies {
    compileOnly("org.ow2.asm:asm:9.1")
    compileOnly("org.ow2.asm:asm-commons:9.1")
    compileOnly("io.netty:netty-all:5.0.0.Alpha2")
    compileOnly("ink.ptms.core:v11200:11200-minimize")
    compileOnly(project(":common:common-core"))
    compileOnly(project(":platform:platform-bukkit"))
}

tasks {
    withType<ShadowJar> {
        archiveClassifier.set("")
        relocate("me.lucko", "taboolib.library")
        relocate("org.objectweb", "taboolib.library")
    }
}