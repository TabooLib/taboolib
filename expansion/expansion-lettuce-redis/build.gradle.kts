import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

// 文档参考: https://www.cnblogs.com/throwable/p/11601538.html
// LettuceGithub： https://github.com/lettuce-io/lettuce-core

dependencies {
    compileOnly("io.lettuce:lettuce-core:6.3.2.RELEASE")
    compileOnly("org.apache.commons:commons-pool2:2.11.1")
    compileOnly(project(":common"))
    compileOnly(project(":common-env"))
    compileOnly(project(":common-util"))
    compileOnly(project(":common-platform-api"))
    compileOnly(project(":module:module-configuration"))
}

tasks {
    withType<ShadowJar> {
        relocate("io.netty.resolver.dns", "io.netty.resolver.dns_4_1_107_final")
    }
}