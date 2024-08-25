@file:Suppress("GradlePackageUpdate", "VulnerableLibrariesLocal")

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

dependencies {
    compileOnly(project(":module:basic-configuration"))
    compileOnly(project(":module:minecraft-chat"))
    // 本体
    compileOnly(project(":module:bukkit-xseries"))
    // 服务端
    compileOnly("ink.ptms.core:v12004:12004-minimize:mapped")
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
    targetCompatibility = JavaVersion.VERSION_17
}