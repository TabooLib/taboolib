dependencies {
    compileOnly(project(":common"))
    compileOnly(project(":common-env"))
    compileOnly(project(":common-util"))
    compileOnly(project(":common-platform-api"))
    compileOnly(project(":platform:platform-bukkit"))
    compileOnly(project(":platform:platform-bukkit-impl"))
    // 服务端
    compileOnly("ink.ptms.core:v12101:12101-minimize:mapped")
    compileOnly("ink.ptms.core:v11604:11604")
    // 数据包
    compileOnly("io.netty:netty-all:5.0.0.Alpha2")
    // Reflex Remapper
    compileOnly("org.ow2.asm:asm:9.6")
    compileOnly("org.ow2.asm:asm-util:9.6")
    compileOnly("org.ow2.asm:asm-commons:9.6")
}

gradle.buildFinished {
    buildDir.deleteRecursively()
}