import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

dependencies {
    implementation("org.tabooproject.reflex:reflex:1.1.8")
    implementation("org.tabooproject.reflex:analyser:1.1.8")
}

tasks {
    withType<ShadowJar> {
        dependencies {
            include(dependency("org.tabooproject.reflex:reflex:1.1.8"))
            include(dependency("org.tabooproject.reflex:analyser:1.1.8"))
        }
        relocate("org.taboooproject", "taboolib.library")
    }
}