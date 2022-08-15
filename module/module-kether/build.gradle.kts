dependencies {
    compileOnly("public:PlaceholderAPI:2.10.9")
    compileOnly("com.google.guava:guava:17.0")
    compileOnly("org.apache.commons:commons-lang3:3.5")
    compileOnly("ink.ptms.core:v11800:11800:api")
    compileOnly(project(":common"))
    compileOnly(project(":common-5"))
    compileOnly(project(":module:module-chat"))
    compileOnly(project(":module:module-lang"))
    compileOnly(project(":module:module-nms-util"))
    compileOnly(project(":module:module-configuration"))
    compileOnly(project(":expansion:expansion-javascript"))
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