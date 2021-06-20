plugins {
    java
    kotlin("jvm") version "1.5.10"
}

repositories {
    maven {
        isAllowInsecureProtocol = true
        url = uri("http://repo.ptms.ink/repository/maven-releases/")
    }
    mavenCentral()
}

dependencies {
    compileOnly("io.izzel.kether:common:1.0.12")
    compileOnly(project(":common"))
    compileOnly(project(":module-chat"))
    compileOnly(project(":module-kether"))
    compileOnly(project(":module-configuration"))
    compileOnly(kotlin("stdlib"))
}