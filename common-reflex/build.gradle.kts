import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

dependencies {
    implementation("org.tabooproject.reflex:reflex:1.0.22")
    implementation("org.tabooproject.reflex:analyser:1.0.22")
}

tasks {
    withType<ShadowJar> {
        dependencies {
            include(dependency("org.tabooproject.reflex:reflex:1.0.22"))
            include(dependency("org.tabooproject.reflex:analyser:1.0.22"))
        }
        relocate("org.taboooproject", "taboolib.library")
    }
}