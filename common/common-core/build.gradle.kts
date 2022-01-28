import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

dependencies {
    implementation("org.tabooproject.reflex:analyser:1.0.4")
    implementation("org.tabooproject.reflex:fast-instance-getter:1.0.4")
    implementation("org.tabooproject.reflex:reflex:1.0.4")
    testImplementation(project(":common:common-impl"))
    testImplementation(project(":common:common-environment"))
}

tasks {
    withType<ShadowJar> {
        archiveClassifier.set("")
        dependencies {
            include(dependency("org.tabooproject.reflex:analyser:1.0.4"))
            include(dependency("org.tabooproject.fast-instance-getter:1.0.4"))
            include(dependency("org.tabooproject.reflex:reflex:1.0.4"))
        }
    }
    build {
        dependsOn(shadowJar)
    }
}
