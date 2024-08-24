import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

dependencies {
    implementation("org.tabooproject.reflex:reflex:1.0.25-test-11")
    implementation("org.tabooproject.reflex:analyser:1.0.25-test-11")
}

tasks {
    withType<ShadowJar> {
        dependencies {
            include(dependency("org.tabooproject.reflex:reflex:1.0.25-test-11"))
            include(dependency("org.tabooproject.reflex:analyser:1.0.25-test-11"))
        }
        relocate("org.taboooproject", "taboolib.library")
    }
}