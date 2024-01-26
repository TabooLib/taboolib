import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

dependencies {
    implementation("org.tabooproject.reflex:reflex:1.0.19")
    implementation("org.tabooproject.reflex:analyser:1.0.19")
}

tasks {
    withType<ShadowJar> {
        dependencies {
            include(dependency("org.tabooproject.reflex:reflex:1.0.19"))
            include(dependency("org.tabooproject.reflex:analyser:1.0.19"))
        }
        relocate("org.taboooproject", "taboolib.library")
    }
}