import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
repositories {
    maven { url = uri("https://repo.papermc.io/repository/maven-public/") }
}

dependencies {
    compileOnly(project(":common"))
    compileOnly(project(":common-util"))
    compileOnly(project(":common-platform-api"))
    compileOnly(project(":platform:platform-velocity"))
    compileOnly("com.velocitypowered:velocity-api:3.1.1")
}