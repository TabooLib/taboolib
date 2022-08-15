import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

dependencies {
    implementation("com.electronwill.night-config:core-conversion:6.0.0")
    compileOnly("org.yaml:snakeyaml:1.28")
    compileOnly("com.typesafe:config:1.4.1")
    compileOnly("com.electronwill.night-config:core:3.6.5")
    compileOnly("com.electronwill.night-config:toml:3.6.5")
    compileOnly("com.electronwill.night-config:json:3.6.5")
    compileOnly("com.electronwill.night-config:hocon:3.6.5")
    compileOnly(project(":common"))
    compileOnly(project(":common-5"))
    compileOnly(project(":module:module-chat"))
    compileOnly("org.tabooproject.reflex:reflex:1.0.19")
    compileOnly("org.tabooproject.reflex:analyser:1.0.19")
}

tasks {
    withType<ShadowJar> {
        archiveClassifier.set("")
        dependencies {
            include(dependency("com.electronwill.night-config:core-conversion:6.0.0"))
        }
        relocate("com.electronwill.nightconfig.core.conversion", "taboolib.library.configuration")
        relocate("org.tabooproject", "taboolib.library")
        minimize()
    }
    build {
        dependsOn(shadowJar)
    }
}