plugins {
    java
    kotlin("jvm") version "1.5.10"
}

repositories {
    mavenCentral()
    maven { url = uri("https://repo.codemc.io/repository/nms/") }
    maven { url = uri("https://repo.nukkitx.com/maven-snapshots") }
    maven { url = uri("https://repo.spongepowered.org/maven") }
    maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots") }
}

dependencies {
    compileOnly("cn.nukkit:nukkit:2.0.0-SNAPSHOT")
    compileOnly("net.md-5:bungeecord-api:1.17-R0.1-SNAPSHOT")
    compileOnly("org.spongepowered:spongeapi:7.2.0")
    compileOnly("org.spigotmc:spigot:1.17-R0.1-20210612.142052-2")
    implementation(project(":common"))
    implementation(kotlin("stdlib"))
}
