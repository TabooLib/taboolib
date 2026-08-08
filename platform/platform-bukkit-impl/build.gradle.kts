@file:Suppress("GradlePackageUpdate", "VulnerableLibrariesLocal")

repositories {
    maven { url = uri("https://jitpack.io") }
    maven { url = uri("https://libraries.minecraft.net") }
    maven { url = uri("https://repo.papermc.io/repository/maven-public/") }
}

dependencies {
    compileOnly(project(":common"))
    compileOnly(project(":common-platform-api"))
    compileOnly(project(":common-util"))
    compileOnly(project(":platform:platform-bukkit"))
    // 仅限 XItemStack
    compileOnly(project(":module:basic:basic-configuration"))
    compileOnly(project(":module:minecraft:minecraft-chat"))

    // 服务端
    compileOnly("ink.ptms.core:v12110:12110:mapped")
    compileOnly("io.paper:folia-api:1.21.4")
    compileOnly("net.md-5:bungeecord-chat:1.20")
    testImplementation(project(":common"))
    testImplementation(project(":common-platform-api"))
    testImplementation(project(":platform:platform-bukkit"))
    testImplementation("io.paper:folia-api:1.21.4")

    // 用于处理命令
    // ClassCastException: Cannot cast java.lang.String to net.kyori.adventure.text.Component
    compileOnly("net.kyori:adventure-api:4.9.2")
    // Mojang API
    compileOnly("com.mojang:brigadier:1.0.18")

    // XSeries
    compileOnly("com.google.code.findbugs:jsr305:3.0.2")
    compileOnly("org.apache.logging.log4j:log4j-api:2.14.1")

    testImplementation(project(":common"))
    testImplementation(project(":common-platform-api"))
    testImplementation(project(":common-util"))
    testImplementation("io.paper:folia-api:1.21.4")
    testImplementation("net.kyori:adventure-api:4.17.0")
    testImplementation("net.kyori:adventure-text-minimessage:4.17.0")
    testImplementation("net.md-5:bungeecord-chat:1.20")
}