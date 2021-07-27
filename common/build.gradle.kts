import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

dependencies {
    implementation("org.ow2.asm:asm:9.2")
    implementation("org.ow2.asm:asm-util:9.2")
    implementation("org.ow2.asm:asm-commons:9.2")
    implementation("me.lucko:jar-relocator:1.5")
}

tasks {
    withType<ShadowJar> {
        archiveClassifier.set("")
        dependencies {
            include(dependency("org.ow2.asm:asm:9.2"))
            include(dependency("org.ow2.asm:asm-util:9.2"))
            include(dependency("org.ow2.asm:asm-commons:9.2"))
            include(dependency("me.lucko:jar-relocator:1.5"))
        }
        relocate("me.lucko", "taboolib.library")
        relocate("org.objectweb", "taboolib.library")
        minimize()
    }
    build {
        dependsOn(shadowJar)
    }
}