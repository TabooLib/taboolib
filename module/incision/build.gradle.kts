dependencies {
    compileOnly(project(":common"))
    compileOnly(project(":common-env"))
    compileOnly(project(":common-platform-api"))
    compileOnly(project(":common-util"))
    compileOnly(project(":common-platform-api"))
    compileOnly(project(":platform:platform-bukkit"))
    compileOnly(project(":platform:platform-bukkit-impl"))
    // 可选 — 检测到时启用 NMS NameResolver
    compileOnly(project(":module:bukkit-nms"))
    // ASM
    compileOnly("org.ow2.asm:asm:9.8")
    compileOnly("org.ow2.asm:asm-commons:9.8")
    compileOnly("org.ow2.asm:asm-util:9.8")
    // self-attach 默认路径
    compileOnly("net.bytebuddy:byte-buddy-agent:1.14.18")
}
