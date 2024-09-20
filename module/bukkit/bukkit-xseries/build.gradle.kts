@file:Suppress("GradlePackageUpdate", "VulnerableLibrariesLocal")

dependencies {
    compileOnly(project(":common"))
    compileOnly(project(":common-platform-api"))
    compileOnly(project(":common-util"))
    compileOnly(project(":module:basic:basic-configuration"))
    compileOnly(project(":module:minecraft:minecraft-chat"))
    // 服务端
    compileOnly("ink.ptms.core:v12004:12004-minimize:mapped")
    compileOnly("ink.ptms.core:v11701:11701-minimize:universal")
    // XSeries
    compileOnly("com.google.code.findbugs:jsr305:3.0.2")
    compileOnly("org.apache.logging.log4j:log4j-api:2.14.1")
}