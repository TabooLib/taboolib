import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("com.github.johnrengelman.shadow") version "7.0.0"
    java
    kotlin("jvm") version "1.5.10"
}

repositories {
//    maven { url = uri("https://maven.aliyun.com/repository/central") }
//    maven { url = uri("https://libraries.minecraft.net" )}
    mavenCentral()
}

dependencies {
//    implementation("me.lucko:commodore:1.10")
    compileOnly(project(":common"))
    compileOnly(kotlin("stdlib"))
}

//tasks {
//    named<ShadowJar>("shadowJar") {
//        archiveClassifier.set("")
//        exclude("**/file/**")
//        dependencies {
//            include(dependency("me.lucko:commodore:1.10"))
//        }
//        relocate("me.lucko", "taboolib.library")
//    }
//    build {
//        dependsOn(shadowJar)
//    }
//}