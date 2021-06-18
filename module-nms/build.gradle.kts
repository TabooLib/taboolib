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
    compileOnly("org.ow2.asm:asm:9.1")
    compileOnly("org.ow2.asm:asm-commons:9.1")
    compileOnly(project(":common"))
    compileOnly(project(":module-dependency"))
    compileOnly(project(":platform-bukkit"))
    compileOnly(kotlin("stdlib"))
}
