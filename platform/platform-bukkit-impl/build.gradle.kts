@file:Suppress("GradlePackageUpdate", "VulnerableLibrariesLocal")

repositories {
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    compileOnly(project(":common"))
//    compileOnly(project(":common-legacy-api"))
    compileOnly(project(":common-platform-api"))
    compileOnly(project(":common-util"))
//    compileOnly(project(":module:module-chat"))
//    compileOnly(project(":module:module-lang"))
//    compileOnly(project(":module:module-configuration"))
//    compileOnly(project(":module:module-bukkit-xseries"))
    compileOnly(project(":platform:platform-bukkit"))
    // 服务端
    compileOnly("ink.ptms.core:v12004:12004-minimize:universal")
    compileOnly("ink.ptms.core:v11200:11200-minimize")
    // 用于处理命令
    // ClassCastException: Cannot cast java.lang.String to net.kyori.adventure.text.Component
    compileOnly("net.kyori:adventure-api:4.9.2")
}