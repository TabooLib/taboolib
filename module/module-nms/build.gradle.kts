dependencies {
    compileOnly(project(":common"))
    compileOnly(project(":common-env"))
    compileOnly(project(":common-util"))
    compileOnly(project(":common-platform-api"))
    compileOnly(project(":module:module-bukkit-util"))
    compileOnly(project(":platform:platform-bukkit"))
    // 服务端
    compileOnly("ink.ptms:nms-all:1.0.0")
    compileOnly("ink.ptms.core:v12000:12000-minimize:mapped")
    compileOnly("ink.ptms.core:v11904:11904-minimize:mapped")
    compileOnly("ink.ptms.core:v11800:11800-minimize:mapped")
    compileOnly("ink.ptms.core:v11200:11200-minimize")
    // DataSerializer
    compileOnly("io.netty:netty-all:5.0.0.Alpha2")
    // Reflex Remapper
    compileOnly("org.ow2.asm:asm:9.4")
    compileOnly("org.ow2.asm:asm-util:9.4")
    compileOnly("org.ow2.asm:asm-commons:9.4")
}