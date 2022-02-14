import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

dependencies {
    implementation("org.tabooproject.reflex:analyser:1.0.9")
    implementation("org.tabooproject.reflex:reflex:1.0.9")
    implementation("org.tabooproject.reflex:fast-instance-getter:1.0.9")
    // Test
    testImplementation(project(":common-core-impl"))
    testImplementation(project(":common-environment"))
}

shrinking {
    shadow = true
}

tasks {
    withType<ShadowJar> {
        dependencies {
            include(dependency("org.tabooproject.reflex:analyser:1.0.9"))
            include(dependency("org.tabooproject.reflex:reflex:1.0.9"))
            include(dependency("org.tabooproject.reflex:fast-instance-getter:1.0.9"))
        }
    }
    build {
        dependsOn("shadowJar")
    }
}