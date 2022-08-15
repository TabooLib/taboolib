import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

dependencies {
    implementation("org.ow2.asm:asm:9.2")
    implementation("org.ow2.asm:asm-util:9.2")
    implementation("org.ow2.asm:asm-commons:9.2")
    implementation("me.lucko:jar-relocator:1.5")
    implementation("org.tabooproject.reflex:reflex:1.0.19")
    implementation("org.tabooproject.reflex:analyser:1.0.19")
}

tasks {
    withType<ShadowJar> {
        archiveClassifier.set("")
        dependencies {
            include(dependency("org.ow2.asm:asm:9.2"))
            include(dependency("org.ow2.asm:asm-util:9.2"))
            include(dependency("org.ow2.asm:asm-commons:9.2"))
            include(dependency("me.lucko:jar-relocator:1.5"))
            include(dependency("org.tabooproject.reflex:reflex:1.0.19"))
            include(dependency("org.tabooproject.reflex:analyser:1.0.19"))
        }
        relocate("me.lucko", "taboolib.library")
        relocate("org.objectweb", "taboolib.library")
        relocate("org.tabooproject", "taboolib.library")
        minimize()
    }
    build {
        dependsOn(shadowJar)
    }
}