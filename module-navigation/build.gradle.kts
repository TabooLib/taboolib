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
    compileOnly("ink.ptms.core:v11600:11600:all")
    compileOnly("ink.ptms.core:v11200:11200:all")
    compileOnly("ink.ptms.core:v11100:11100:all")
    compileOnly("ink.ptms.core:v10900:10900:all")
    compileOnly(project(":common"))
    compileOnly(project(":module-nms"))
    compileOnly(kotlin("stdlib"))
}
