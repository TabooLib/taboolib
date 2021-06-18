plugins {
    java
    kotlin("jvm") version "1.5.10"
}

repositories {
    maven {
        isAllowInsecureProtocol = true
        url = uri("http://repo.ptms.ink/repository/maven-releases/")
    }
    maven {
        url = uri("https://repo.codemc.io/repository/nms/")
    }
    mavenCentral()
}

dependencies {
    compileOnly("ink.ptms.core:v11200:11200:all")
    compileOnly("ink.ptms.core:v11605:11605")
    compileOnly("ink.ptms.core:v11600:11600:all")
    compileOnly("ink.ptms.core:v11500:11500:all")
    compileOnly("ink.ptms.core:v11400:11400:all")
    compileOnly("ink.ptms.core:v11300:11300:all")
    compileOnly("ink.ptms.core:v10900:10900:all")
    compileOnly("ink.ptms.core:v10800:10800:all")
    compileOnly("org.spigotmc:spigot:1.17-R0.1-20210612.142052-2")
    compileOnly("org.ow2.asm:asm:9.1")
    compileOnly("org.ow2.asm:asm-commons:9.1")
    compileOnly(project(":common"))
    compileOnly(project(":module-dependency"))
    compileOnly(project(":platform-bukkit"))
    compileOnly(kotlin("stdlib"))
}
