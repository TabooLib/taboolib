dependencies {
    implementation("org.yaml:snakeyaml:1.28")
    compileOnly(project(":common:common-core"))
    compileOnly(project(":common:common-util"))
    compileOnly(project(":module:module-chat"))
}

tasks {
    withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
        archiveClassifier.set("")
        archiveBaseName.set("${archiveBaseName.get()}-shaded")
        dependencies {
            include(dependency("org.yaml:snakeyaml:1.28"))
        }
    }
    build {
        dependsOn(shadowJar)
    }
}