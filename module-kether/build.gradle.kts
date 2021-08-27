dependencies {
    compileOnly("public:PlaceholderAPI:2.10.9")
    compileOnly("org.spigotmc:spigot:1.12.2-R0.1-20190527.155359-34")
//    compileOnly("ink.ptms.core:v11200:11200:all")
    compileOnly("com.google.guava:guava:17.0")
    compileOnly("org.apache.commons:commons-lang3:3.5")
    implementation(project(":common"))
    compileOnly(project(":common-5"))
    compileOnly(project(":module-chat"))
    compileOnly(project(":module-lang"))
    compileOnly(project(":module-nms-util"))
    compileOnly(project(":module-configuration"))
}