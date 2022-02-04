import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

dependencies {
    implementation("org.yaml:snakeyaml:1.28")
    implementation("com.typesafe:config:1.4.1")
    implementation("com.electronwill.night-config:core:3.6.5")
    implementation("com.electronwill.night-config:toml:3.6.5")
    implementation("com.electronwill.night-config:json:3.6.5")
    implementation("com.electronwill.night-config:hocon:3.6.5")
    implementation(project(":module:module-configuration"))
}

tasks {
    withType<ShadowJar> {
        archiveClassifier.set("")
    }
    build {
        dependsOn(shadowJar)
    }
}