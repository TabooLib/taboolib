dependencies {
    compileOnly("com.google.guava:guava:21.0")
    compileOnly("com.google.code.gson:gson:2.8.7")
    compileOnly("ink.ptms:nms-all:1.0.0")
    compileOnly("ink.ptms.core:v11801:11801:api")
    compileOnly("ink.ptms.core:v11800:11800-minimize:mapped")
    compileOnly(project(":common:common-core"))
    compileOnly(project(":module:module-nms"))
    compileOnly(project(":platform:platform-bukkit"))
}