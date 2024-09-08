@file:Suppress("GradlePackageUpdate", "VulnerableLibrariesLocal")

repositories {
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    compileOnly(project(":common"))
    compileOnly(project(":common-util"))
    compileOnly(project(":common-platform-api"))
    compileOnly(project(":platform:platform-bukkit"))
    // 服务端
    compileOnly("ink.ptms.core:v12004:12004-minimize:universal")
    // 依赖插件
    compileOnly("net.milkbowl.vault:Vault:1")
    compileOnly("public:PlaceholderAPI:2.10.9")
}