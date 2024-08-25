import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

dependencies {
    implementation("org.tabooproject.reflex:reflex:1.1.1")
    implementation("org.tabooproject.reflex:analyser:1.1.1")
}

tasks {
    withType<ShadowJar> {
        dependencies {
            include(dependency("org.tabooproject.reflex:reflex:1.1.1"))
            include(dependency("org.tabooproject.reflex:analyser:1.1.1"))
        }
        relocate("org.taboooproject", "taboolib.library")
    }
}