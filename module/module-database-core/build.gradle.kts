import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

dependencies {
    compileOnly("com.zaxxer:HikariCP:4.0.3")
    compileOnly(project(":common"))
    compileOnly(project(":module:module-configuration"))
}

tasks {
    withType<ShadowJar> {
        archiveClassifier.set("")
    }
    build {
        dependsOn(shadowJar)
    }
}