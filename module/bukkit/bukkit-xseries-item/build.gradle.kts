@file:Suppress("GradlePackageUpdate", "VulnerableLibrariesLocal")

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

dependencies {
    compileOnly(project(":common"))
    compileOnly(project(":common-platform-api"))
    compileOnly(project(":common-util"))
    compileOnly(project(":module:basic:basic-configuration"))
    compileOnly(project(":module:bukkit:bukkit-util"))
    compileOnly(project(":module:minecraft:minecraft-chat"))
    // 本体
    compileOnly(project(":module:bukkit:bukkit-xseries"))
    // 服务端
    compileOnly("ink.ptms.core:v12004:12004-minimize:mapped")
    compileOnly("net.md-5:bungeecord-chat:1.20")
     compileOnly("com.mojang:authlib:5.0.51")
    // XSeries
    compileOnly("com.google.code.findbugs:jsr305:3.0.2")
    compileOnly("org.apache.logging.log4j:log4j-api:2.14.1")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "17"
    }
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_1_8
}