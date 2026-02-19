import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

dependencies {
    compileOnly(project(":common"))
    compileOnly(project(":common-util"))
    compileOnly(project(":module:minecraft:minecraft-chat"))
    compileOnly(project(":module:bukkit-nms"))
    compileOnly(project(":module:bukkit-nms:bukkit-nms-tag"))
    // 服务端
    compileOnly("ink.ptms.core:v12005:12005:mapped")
}

tasks {
    withType<ShadowJar> {
        archiveClassifier.set("")
        relocate("org.tabooproject", "taboolib.library")
    }
    build {
        dependsOn(shadowJar)
    }
}