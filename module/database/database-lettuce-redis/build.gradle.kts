import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

// 文档参考: https://www.cnblogs.com/throwable/p/11601538.html
// LettuceGithub： https://github.com/lettuce-io/lettuce-core

dependencies {
    // 使用 api 传递依赖
    api("io.lettuce:lettuce-core:7.2.1.RELEASE")
    compileOnly("org.apache.commons:commons-pool2:2.12.1")

    compileOnly(project(":common"))
    compileOnly(project(":common-env"))
    compileOnly(project(":common-util"))
    compileOnly(project(":common-platform-api"))
    compileOnly(project(":module:basic:basic-configuration"))
}

tasks {
    withType<ShadowJar> {
        relocate("io.netty.", "io.netty_4_2_5_final.")
        relocate("org.apache.commons.pool2.", "org.apache.commons.pool2_2_12_1.")
        relocate("reactor.", "reactor_3_6_6.")
        relocate("org.reactivestreams.", "org.reactivestreams_1_0_4.")
        relocate("org.slf4j.", "org.slf4j_1_7_36.")
        relocate("redis.clients.authentication.", "redis.clients.authentication_0_1_1_beta2.")
    }
}