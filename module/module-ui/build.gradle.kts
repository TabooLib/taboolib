dependencies {
    implementation(project(":module:module-ui-legacy"))
    compileOnly("ink.ptms.core:v11904:11904-minimize:mapped")
    compileOnly("ink.ptms.core:v11600:11600-minimize")
    compileOnly("ink.ptms.core:v11200:11200-minimize")
    compileOnly("ink.ptms.core:v10900:10900")
    compileOnly(project(":common"))
    compileOnly(project(":module:module-chat"))
    compileOnly(project(":module:module-nms"))
    compileOnly(project(":platform:platform-bukkit"))
}