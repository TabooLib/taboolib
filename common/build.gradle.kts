import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

dependencies {
    implementation("org.ow2.asm:asm:9.4")
    implementation("org.ow2.asm:asm-util:9.4")
    implementation("org.ow2.asm:asm-commons:9.4")
    implementation("me.lucko:jar-relocator:1.5")
    // 当时改这个是为了支持 kotlin-relfect 结果失败了忘记恢复，导致 Art 脚本系统全面崩溃
    // implementation("com.github.bkm016:jar-relocator:1.7-R2")
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