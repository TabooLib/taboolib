import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

dependencies {
    implementation("org.yaml:snakeyaml:2.0")
    implementation("com.typesafe:config:1.4.2")
    implementation("com.electronwill.night-config:core:3.6.7")
    implementation("com.electronwill.night-config:toml:3.6.7")
    implementation("com.electronwill.night-config:json:3.6.7")
    implementation("com.electronwill.night-config:hocon:3.6.7")
    implementation(project(":module:module-configuration"))
}

tasks {
    withType<ShadowJar> {
        archiveClassifier.set("")
        relocate("org.yaml.snakeyaml.", "org.yaml.snakeyaml_2_0.")
    }
    build {
        dependsOn(shadowJar)
    }
}