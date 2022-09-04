dependencies {
    compileOnly("org.spigotmc:spigot:1.16.5-R0.1-20210611.090701-17")
//    compileOnly("ink.ptms.core:v11605:11605")
    compileOnly(project(":common"))
    compileOnly(project(":module:module-nms"))
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