dependencies {
    compileOnly(project(":common"))
    compileOnly(project(":common-env"))
    compileOnly(project(":common-util"))
    compileOnly(project(":common-platform-api"))
    compileOnly(project(":module:module-bukkit-util"))
    compileOnly(project(":platform:platform-bukkit"))
    // 服务端
    compileOnly("ink.ptms.core:v12005:12005:mapped")
    compileOnly("ink.ptms.core:v11904:11904-minimize:mapped")
    compileOnly("ink.ptms.core:v11800:11800-minimize:mapped")
    compileOnly("ink.ptms.core:v11604:11604")
    compileOnly("ink.ptms.core:v11300:11300")
    compileOnly("ink.ptms.core:v11200:11200-minimize")
    compileOnly("ink.ptms.core:v10900:10900")
    compileOnly("ink.ptms.core:v10800:10800")
    // DataSerializer
    compileOnly("io.netty:netty-all:5.0.0.Alpha2")
    // Reflex Remapper
    compileOnly("org.ow2.asm:asm:9.6")
    compileOnly("org.ow2.asm:asm-util:9.6")
    compileOnly("org.ow2.asm:asm-commons:9.6")
}