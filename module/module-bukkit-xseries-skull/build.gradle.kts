@file:Suppress("GradlePackageUpdate", "VulnerableLibrariesLocal")

repositories {
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    compileOnly(project(":common"))
    compileOnly(project(":module:module-chat"))
    compileOnly(project(":module:module-configuration"))
    compileOnly(project(":module:module-bukkit-xseries"))
    compileOnly(project(":platform:platform-bukkit"))
    // 服务端
    compileOnly("ink.ptms.core:v12004:12004-minimize:mapped")
    compileOnly("com.mojang:authlib:5.0.51")
    // XSeries
    compileOnly("com.google.code.findbugs:jsr305:3.0.2")
    compileOnly("org.apache.logging.log4j:log4j-api:2.14.1")
}

// java 17
tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("--release", "17"))
}