@file:Suppress("GradlePackageUpdate", "VulnerableLibrariesLocal")

repositories {
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    compileOnly(project(":module:module-chat"))
    compileOnly(project(":module:module-configuration"))
    compileOnly(project(":platform:platform-bukkit"))
    // 服务端
    compileOnly("ink.ptms.core:v12004:12004-minimize:universal")
    compileOnly("ink.ptms.core:v11200:11200-minimize")
}