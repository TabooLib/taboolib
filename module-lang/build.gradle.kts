plugins {
    java
    kotlin("jvm") version "1.5.10"
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(project(":common"))
    compileOnly(project(":module-chat"))
    compileOnly(project(":module-kether"))
    compileOnly(project(":module-configuration"))
    compileOnly(kotlin("stdlib"))
}