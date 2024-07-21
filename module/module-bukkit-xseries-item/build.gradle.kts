@file:Suppress("GradlePackageUpdate", "VulnerableLibrariesLocal")

repositories {
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    compileOnly(project(":common"))
    compileOnly(project(":module:module-chat"))
    compileOnly(project(":module:module-configuration"))
    compileOnly(project(":module:module-bukkit-xseries"))
    compileOnly(project(":module:module-bukkit-xseries-skull"))
    compileOnly(project(":platform:platform-bukkit"))
    // 服务端
    compileOnly("ink.ptms.core:v12004:12004-minimize:mapped")
    compileOnly("ink.ptms.core:v11701:11701-minimize:universal")
    // XSeries
    compileOnly("com.google.code.findbugs:jsr305:3.0.2")
    compileOnly("org.apache.logging.log4j:log4j-api:2.14.1")
}