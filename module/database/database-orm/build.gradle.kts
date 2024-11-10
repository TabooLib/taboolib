import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

dependencies {
    compileOnly("com.zaxxer:HikariCP:4.0.3")
    compileOnly("com.j256.ormlite:ormlite-core:6.1")
    compileOnly("com.j256.ormlite:ormlite-jdbc:6.1")
    compileOnly(project(":common"))
    compileOnly(project(":common-env"))
    compileOnly(project(":common-reflex"))
    compileOnly(project(":common-platform-api"))
    compileOnly(project(":common-util"))
    compileOnly(project(":module:basic:basic-configuration"))
    compileOnly(project(":module:database"))
}

tasks {
    withType<ShadowJar> {
        relocate("com.j256.ormlite.", "com.j256.ormlite_6_0.")
        relocate("com.zaxxer.hikari.", "com.zaxxer.hikari_4_0_3.")
    }
}
