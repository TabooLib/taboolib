import org.spongepowered.gradle.plugin.config.PluginLoaders
import org.spongepowered.plugin.metadata.PluginDependency

plugins {
    java
    `java-library`
    kotlin("jvm") version "1.5.10"
    id("org.spongepowered.gradle.plugin") version "1.1.1"
}

repositories {
    mavenCentral()
    maven { url = uri("https://repo.spongepowered.org/maven") }
}

//sponge {
//    apiVersion("8.0.0")
//    plugin("platform-sponge-api8") {
//        loader(PluginLoaders.JAVA_PLAIN)
//        displayName("@plugin_name@")
//        version("@plugin_version@")
//        mainClass("taboolib.platform.SpongePlugin")
//        description("taboolib")
//        links {
//            homepage("https://spongepowered.org")
//            source("https://spongepowered.org/source")
//            issues("https://spongepowered.org/issues")
//        }
//        contributor("Eric12324") {
//            description("Lead Developer")
//        }
//        dependency("spongeapi") {
//            loadOrder(PluginDependency.LoadOrder.AFTER)
//            optional(false)
//        }
//    }
//}

dependencies {
    // this will not work, you must use the sponge gradle plugin
    // compileOnly("org.spongepowered:spongeapi:8.0.0-SNAPSHOT")
    compileOnly(project(":common"))
    compileOnly(kotlin("stdlib"))
}
