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
    api("org.spigotmc:spigot:1.17-R0.1-20210612.142052-2")
    implementation(project(":common"))
    implementation(kotlin("stdlib"))
}
