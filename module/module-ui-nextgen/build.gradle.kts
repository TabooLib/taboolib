import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

dependencies {
    compileOnly("org.spigotmc:spigot:1.16.5-R0.1-20210611.090701-17")
//    compileOnly("ink.ptms.core:v11605:11605")
    compileOnly(project(":common"))
    compileOnly(project(":module:module-nms"))
    compileOnly(project(":module:module-nms-util"))
    compileOnly(project(":platform:platform-bukkit"))
}
