plugins {
    java
    kotlin("jvm") version "1.5.10"
}

repositories {
    maven { url = uri("https://repo.codemc.io/repository/nms/") }
    mavenCentral()
}

dependencies {
    compileOnly("org.spigotmc:spigot:1.17-R0.1-20210612.142052-2")
    compileOnly(project(":common"))
    compileOnly(project(":common-5"))
    compileOnly(kotlin("stdlib"))
}
