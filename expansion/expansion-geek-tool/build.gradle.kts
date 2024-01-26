dependencies {
    compileOnly(project(":common"))
    compileOnly(project(":common-util"))
    compileOnly(project(":common-legacy-api"))
    compileOnly(project(":common-platform-api"))
    // 服务端
    compileOnly("ink.ptms.core:v11600:11600-minimize")
    compileOnly("ink.ptms.core:v11701:11701-minimize:universal")
}