@file:Suppress("VulnerableLibrariesLocal")

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

dependencies {
    compileOnly(project(":common"))
    compileOnly(project(":common-env"))
    compileOnly(project(":common-util"))
    compileOnly(project(":common-legacy-api"))
    compileOnly(project(":common-platform-api"))
    compileOnly(project(":module:minecraft:minecraft-chat"))
    compileOnly(project(":module:minecraft:minecraft-i18n"))
    compileOnly(project(":module:bukkit-nms"))
    compileOnly(project(":module:bukkit-nms:bukkit-nms-stable"))
    compileOnly(project(":module:basic:basic-configuration"))
    compileOnly(project(":module:script:script-javascript"))
    // 扩展
    compileOnly("public:PlaceholderAPI:2.10.9")
    // 解析
    compileOnly("com.mojang:datafixerupper:4.0.26")
    // 表达式
    compileOnly("org.apache.commons:commons-jexl3:3.2.1")
    // 服务端
    compileOnly("ink.ptms.core:v12004:12004-minimize:mapped")
}

tasks {
    withType<ShadowJar> {
        archiveClassifier.set("")
        relocate("org.tabooproject", "taboolib.library")
        relocate("org.apache.commons.jexl3", "org.apache.commons.jexl3_3_2_1")
    }
    build {
        dependsOn(shadowJar)
    }
}