dependencies {
    compileOnly(project(":common"))
    compileOnly(project(":common-util"))
    compileOnly(project(":common-platform-api"))
    compileOnly(project(":module:minecraft-chat"))
    compileOnly(project(":module:nms"))
    compileOnly(project(":module:bukkit-util"))
    compileOnly(project(":module:bukkit-xseries"))
    compileOnly(project(":platform:platform-bukkit"))
    // 服务端
    compileOnly("ink.ptms.core:v11904:11904-minimize:mapped")
    compileOnly("ink.ptms.core:v11600:11600-minimize")
    compileOnly("ink.ptms.core:v11200:11200-minimize")
    compileOnly("ink.ptms.core:v10900:10900")
    // 低版本兼容
    implementation(project(":module:bukkit-ui-legacy"))
    // 高版本兼容
    implementation(project(":module:bukkit-ui-modern"))
}