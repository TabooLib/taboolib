@file:Suppress("GradlePackageUpdate", "VulnerableLibrariesLocal")

repositories {
    maven { url = uri("https://jitpack.io") }
    maven { url = uri("https://libraries.minecraft.net") }
}

dependencies {
    compileOnly(project(":common"))
    compileOnly(project(":common-platform-api"))
    compileOnly(project(":common-util"))
    compileOnly(project(":platform:platform-bukkit"))
    compileOnly(project(":expansion:expansion-folia"))
    // 服务端
    compileOnly("ink.ptms.core:v12004:12004-minimize:universal")
    compileOnly("io.paper:folia-api:1.20.4")
    compileOnly("net.md-5:bungeecord-chat:1.20")
    // 用于处理命令
    // ClassCastException: Cannot cast java.lang.String to net.kyori.adventure.text.Component
    compileOnly("net.kyori:adventure-api:4.9.2")
    // Mojang API
    compileOnly("com.mojang:brigadier:1.0.18")
}