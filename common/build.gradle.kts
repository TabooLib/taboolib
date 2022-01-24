import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

dependencies {
    implementation("org.ow2.asm:asm:9.2")
    implementation("org.ow2.asm:asm-util:9.2")
    implementation("org.ow2.asm:asm-commons:9.2")
    implementation("me.lucko:jar-relocator:1.5")
    implementation("org.tabooproject.reflex:analyser:1.0.3")
    implementation("org.tabooproject.reflex:fast-instance-getter:1.0.3")
    implementation("org.tabooproject.reflex:reflex:1.0.3")
}

tasks {
    withType<ShadowJar> {
        archiveClassifier.set("")
        dependencies {
            include(dependency("org.ow2.asm:asm:9.2"))
            include(dependency("org.ow2.asm:asm-util:9.2"))
            include(dependency("org.ow2.asm:asm-commons:9.2"))
            include(dependency("me.lucko:jar-relocator:1.5"))
            include(dependency("org.tabooproject.reflex:analyser:1.0.3"))
            include(dependency("org.tabooproject.fast-instance-getter:1.0.3"))
            include(dependency("org.tabooproject.reflex:reflex:1.0.3"))
        }
        relocate("me.lucko", "taboolib.library")
        relocate("org.objectweb", "taboolib.library")
        minimize()
    }
    build {
        dependsOn(shadowJar)
    }
}