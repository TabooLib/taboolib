@file:Suppress("VulnerableLibrariesLocal")

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

repositories {
    maven("https://repo.spongepowered.org/maven")
}

dependencies {
    compileOnly(project(":common"))
    compileOnly(project(":common-env"))
    compileOnly(project(":common-util"))
    compileOnly(project(":common-legacy-api"))
    compileOnly(project(":common-platform-api"))
    compileOnly(project(":module:module-chat"))
    compileOnly(project(":module:module-lang"))
    compileOnly(project(":module:module-nms-util"))
    compileOnly(project(":module:module-nms-util-unstable"))
    compileOnly(project(":module:module-configuration"))
    compileOnly(project(":expansion:expansion-javascript"))
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
        relocate("org.apache.commons.jexl3", "org.apache.commons.jexl3_3_2_1")
    }
}