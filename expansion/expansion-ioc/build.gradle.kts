@file:Suppress("GradlePackageUpdate")

dependencies {
    compileOnly("com.google.code.gson:gson:2.8.7")
    compileOnly(project(":common"))
    compileOnly(project(":module:module-configuration"))
    compileOnly("ink.ptms.core:v11701:11701-minimize:universal")
    compileOnly(project(":platform:platform-bukkit"))
    compileOnly("org.tabooproject.reflex:reflex:1.0.19")
    compileOnly("org.tabooproject.reflex:analyser:1.0.19")
}
tasks {
    withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
        archiveClassifier.set("")
        relocate("org.tabooproject", "taboolib.library")
    }
    build {
        dependsOn(shadowJar)
    }
}