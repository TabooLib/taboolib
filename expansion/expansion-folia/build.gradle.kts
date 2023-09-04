dependencies {
    compileOnly(project(":common"))
    compileOnly(project(":common-5"))
    compileOnly(project(":module:module-nms-util"))
    compileOnly(project(":module:module-nms"))
    compileOnly(project(":module:module-chat"))
    compileOnly(project(":platform:platform-bukkit"))
//    compileOnly("dev.folia:folia-api:1.20.1-R0.1-SNAPSHOT")
    compileOnly(fileTree("libs"))
}
