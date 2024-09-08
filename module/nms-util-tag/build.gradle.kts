import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

dependencies {
    compileOnly(project(":common"))
    compileOnly(project(":common-util"))
    compileOnly(project(":common-legacy-api")) // Coerce
    compileOnly(project(":common-platform-api"))
    compileOnly(project(":module:minecraft-chat"))
    compileOnly(project(":module:bukkit-util")) // isAir
    compileOnly(project(":module:nms"))
    // 服务端
    compileOnly("ink.ptms.core:v12005:12005:universal")
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