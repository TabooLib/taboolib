import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

dependencies {
    compileOnly("ink.ptms.core:v11200:11200:all")
    compileOnly("org.ow2.asm:asm:9.1")
    compileOnly("org.ow2.asm:asm-commons:9.1")
    compileOnly(project(":common"))
}

tasks {
    withType<ShadowJar> {
        archiveClassifier.set("")
        relocate("me.lucko", "taboolib.library")
        relocate("org.objectweb", "taboolib.library")
    }
    build {
        dependsOn(shadowJar)
    }
}