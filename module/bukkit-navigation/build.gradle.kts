dependencies {
    compileOnly(project(":common"))
    compileOnly(project(":module:nms"))
    // 服务端
    compileOnly("ink.ptms.core:v12101:12101-minimize:universal")
    // getBlockHeight 用到
    compileOnly("ink.ptms.core:v11200:11200-minimize")
    compileOnly("ink.ptms.core:v11100:11100")
    compileOnly("ink.ptms.core:v10900:10900")
}
