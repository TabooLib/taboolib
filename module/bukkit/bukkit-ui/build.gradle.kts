dependencies {
    compileOnly(project(":common"))
    compileOnly(project(":common-util"))
    compileOnly(project(":common-platform-api"))
    compileOnly(project(":module:minecraft:minecraft-chat"))
    compileOnly(project(":module:bukkit-nms"))
    compileOnly(project(":module:bukkit:bukkit-util"))
    compileOnly(project(":module:bukkit:bukkit-xseries"))
    compileOnly(project(":platform:platform-bukkit"))
    // 服务端
    compileOnly("ink.ptms.core:v11904:11904-minimize:mapped")
    compileOnly("ink.ptms.core:v11600:11600-minimize")
    compileOnly("ink.ptms.core:v11200:11200-minimize")
    compileOnly("ink.ptms.core:v10900:10900")
    // 版本实现
    compileOnly(project(":module:bukkit:bukkit-ui:bukkit-ui-12100"))
    compileOnly(project(":module:bukkit:bukkit-ui:bukkit-ui-legacy"))
}