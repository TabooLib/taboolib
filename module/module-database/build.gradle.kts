import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

dependencies {
    compileOnly("com.zaxxer:HikariCP:4.0.3")
    compileOnly(project(":common"))
    compileOnly(project(":common-env"))
    compileOnly(project(":common-platform-api"))
    compileOnly(project(":common-util"))
    compileOnly(project(":module:module-configuration"))
}

tasks {
    withType<ShadowJar> {
        relocate("com.zaxxer.hikari.", "com.zaxxer.hikari_4_0_3.")
    }
}