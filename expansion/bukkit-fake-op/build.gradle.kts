import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

dependencies {
    compileOnly(project(":common"))
    compileOnly(project(":common-env"))
    compileOnly(project(":common-util"))
    compileOnly(project(":module:bukkit-nms"))
    compileOnly("ink.ptms.core:v11701:11701-minimize:universal")
    compileOnly("net.bytebuddy:byte-buddy:1.14.9")
}

tasks.withType<ShadowJar> {
    relocate("net.bytebuddy.", "net.bytebuddy_1_14_9.")
}