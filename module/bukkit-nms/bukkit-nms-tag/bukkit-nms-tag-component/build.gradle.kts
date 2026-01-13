dependencies {
    compileOnly(project(":common"))
    compileOnly(project(":common-env"))
    compileOnly(project(":common-util"))
    compileOnly(project(":module:bukkit-nms"))
    compileOnly("com.mojang:brigadier:1.0.500")
    compileOnly(project(":common-platform-api"))
    compileOnly("ink.ptms.core:v12107:12107:mapped")
    compileOnly(project(":platform:platform-bukkit"))
    compileOnly(project(":platform:platform-bukkit-impl"))
    compileOnly(project(":module:bukkit-nms:bukkit-nms-tag"))
    compileOnly("com.mojang:authlib:7.1.61")
    compileOnly("it.unimi.dsi:fastutil:8.5.15")
}