import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

dependencies {
    implementation("org.tabooproject.reflex:reflex:1.2.6")
    implementation("org.tabooproject.reflex:analyser:1.2.6")
}

tasks {
    withType<ShadowJar> {
        dependencies {
            include(dependency("org.tabooproject.reflex:reflex:1.2.6"))
            include(dependency("org.tabooproject.reflex:analyser:1.2.6"))
        }
        relocate("org.taboooproject", "taboolib.library")
    }
}