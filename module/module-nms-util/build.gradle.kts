dependencies {
    compileOnly("com.google.guava:guava:21.0")
    compileOnly("com.google.code.gson:gson:2.8.7")
    compileOnly("ink.ptms:nms-all:1.0.0")
    compileOnly("ink.ptms.core:v11802:11802-minimize:mapped")
    compileOnly("ink.ptms.core:v11802:11802-minimize:universal")
    compileOnly(project(":common"))
    compileOnly(project(":module:module-nms"))
    compileOnly(project(":platform:platform-bukkit"))
}