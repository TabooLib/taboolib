plugins {
    java
    kotlin("jvm") version "1.5.10"
}

repositories {
    maven { url = uri("https://maven.aliyun.com/repository/central") }
    mavenCentral()
}

dependencies {
    compileOnly("com.zaxxer:HikariCP:4.0.3")
    compileOnly(project(":common"))
    compileOnly(project(":module-configuration"))
    compileOnly(kotlin("stdlib"))
}