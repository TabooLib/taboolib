plugins {
    java
    kotlin("jvm") version "1.5.10"
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://repo.codemc.io/repository/nms/")
    }
}

dependencies {
    compileOnly("org.spigotmc:spigot:1.17-R0.1-20210612.142052-2")
    compileOnly(project(":common"))
    compileOnly(kotlin("stdlib"))
}
