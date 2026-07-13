import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

dependencies {
    compileOnlyApi(project(":common"))
    compileOnlyApi(project(":common-env"))
    compileOnlyApi(project(":common-platform-api"))
    compileOnlyApi(project(":common-util"))
    compileOnlyApi(project(":module:basic:basic-configuration"))
    compileOnlyApi("com.zaxxer:HikariCP:4.0.3")

    testImplementation(project(":common-util"))
    testImplementation("com.zaxxer:HikariCP:4.0.3")
    testImplementation("org.xerial:sqlite-jdbc:3.42.0.0")
}

tasks {
    withType<ShadowJar> {
        relocate("com.zaxxer.hikari.", "com.zaxxer.hikari_4_0_3.")
    }
}
