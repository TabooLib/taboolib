import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

dependencies {
    implementation("org.tabooproject.reflex:analyser:1.0.5")
    implementation("org.tabooproject.reflex:reflex:1.0.5")
    implementation("org.tabooproject.reflex:fast-instance-getter:1.0.5")
    // Test
    testImplementation(project(":common:common-core-impl"))
    testImplementation(project(":common:common-environment"))
}

shrinking {
    shadow = true
}

tasks {
    withType<ShadowJar> {
        archiveClassifier.set("")
        dependencies {
            include(dependency("org.tabooproject.reflex:analyser:1.0.5"))
            include(dependency("org.tabooproject.reflex:reflex:1.0.5"))
            include(dependency("org.tabooproject.reflex:fast-instance-getter:1.0.5"))
        }
    }
}