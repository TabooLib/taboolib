dependencies {
    compileOnly("com.google.guava:guava:31.1-jre")
    compileOnly("ink.ptms:nms-all:1.0.0")
    compileOnly("ink.ptms.core:v11701:11701-minimize:universal")
    compileOnly(project(":common"))
    compileOnly(project(":module:module-nms"))
}
repositories {
    mavenCentral()
}
