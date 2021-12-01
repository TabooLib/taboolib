dependencies {
    compileOnly("public:PlaceholderAPI:2.10.9")
    compileOnly("com.google.guava:guava:17.0")
    compileOnly("org.apache.commons:commons-lang3:3.5")
    compileOnly("ink.ptms.core:v11200:11200-minimize")
    implementation(project(":common"))
    compileOnly(project(":common-5"))
    compileOnly(project(":module:module-chat"))
    compileOnly(project(":module:module-lang"))
    compileOnly(project(":module:module-nms-util"))
    compileOnly(project(":module:module-configuration"))
}