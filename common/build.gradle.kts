import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

dependencies {
    implementation("org.ow2.asm:asm:9.4")
    implementation("org.ow2.asm:asm-util:9.4")
    implementation("org.ow2.asm:asm-commons:9.4")
    implementation("com.github.bkm016:jar-relocator:1.7-R2")
    implementation("org.tabooproject.reflex:reflex:1.0.19")
    implementation("org.tabooproject.reflex:analyser:1.0.19")
}

tasks {
    withType<ShadowJar> {
        archiveClassifier.set("")
        dependencies {
            include(dependency("org.ow2.asm:asm:9.4"))
            include(dependency("org.ow2.asm:asm-util:9.4"))
            include(dependency("org.ow2.asm:asm-commons:9.4"))
            include(dependency("com.github.bkm016:jar-relocator:1.7-R2"))
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