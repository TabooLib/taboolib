@file:Suppress("GradlePackageUpdate", "VulnerableLibrariesLocal")

repositories {
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    compileOnly(project(":common"))
    compileOnly(project(":common-legacy-api"))
    compileOnly(project(":common-platform-api"))
    compileOnly(project(":common-util"))
    compileOnly(project(":module:basic:basic-configuration"))
    compileOnly(project(":module:minecraft:minecraft-chat"))
    compileOnly(project(":module:minecraft:minecraft-i18n")) // 注册 TypeBossBar
    compileOnly(project(":platform:platform-bukkit-impl"))
    compileOnly(project(":platform:platform-bukkit"))
    // 服务端
    compileOnly("ink.ptms.core:v12107:12107-minimize:universal")
    compileOnly("ink.ptms.core:v12101:12101-minimize:universal")
    compileOnly("ink.ptms.core:v11200:11200-minimize")
}