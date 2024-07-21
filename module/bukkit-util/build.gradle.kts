@file:Suppress("GradlePackageUpdate", "VulnerableLibrariesLocal")

repositories {
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    compileOnly(project(":common"))
    compileOnly(project(":common-legacy-api"))
    compileOnly(project(":common-platform-api"))
    compileOnly(project(":common-util"))
    compileOnly(project(":module:basic-configuration"))
    compileOnly(project(":module:minecraft-chat"))
    compileOnly(project(":module:minecraft-i18n")) // 注册 TypeBossBar
    compileOnly(project(":module:bukkit-xseries"))
    compileOnly(project(":module:bukkit-xseries-skull"))
    compileOnly(project(":platform:platform-bukkit"))
    // 服务端
    compileOnly("ink.ptms.core:v12100:12100-minimize:universal")
    compileOnly("ink.ptms.core:v11200:11200-minimize")
}

tasks.named("compileJava") {
    dependsOn(":module:bukkit-xseries-skull:jar")
}