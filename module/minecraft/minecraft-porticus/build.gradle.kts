dependencies {
    compileOnly("ink.ptms.core:v12004:12004-minimize:mapped")
    compileOnly("net.md_5.bungee:BungeeCord:1")
    compileOnly(project(":common"))
    compileOnly(project(":common-util"))
    compileOnly(project(":common-env"))
    compileOnly(project(":common-platform-api"))
    testImplementation(project(":common"))
    testImplementation(project(":common-util"))
    testImplementation(project(":common-platform-api"))
    testImplementation("ink.ptms.core:v12004:12004-minimize:mapped")
}