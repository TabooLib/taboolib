plugins {
    java
    kotlin("jvm") version "1.5.10"
}

repositories {
    maven { url = uri("https://maven.aliyun.com/repository/central") }
    maven { url = uri("https://repo.codemc.io/repository/maven-public/") }
    maven { url = uri("https://repo.codemc.io/repository/nms/") }
    maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots") }
    mavenCentral()
}

dependencies {
    compileOnly("com.github.ben-manes.caffeine:caffeine:3.0.2")
    compileOnly("net.md-5:bungeecord-bootstrap:1.17-R0.1-20210614.231150-8@jar")
    compileOnly("org.spigotmc:spigot:1.17-R0.1-20210612.142052-2")
    compileOnly(project(":common"))
    compileOnly(project(":module-dependency"))
    compileOnly(kotlin("stdlib"))
}