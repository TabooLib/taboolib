import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

dependencies {
    compileOnly("redis.clients:jedis:4.2.3")
    compileOnly(project(":common"))
    compileOnly(project(":common-env"))
    compileOnly(project(":common-util"))
    compileOnly(project(":common-platform-api"))
    compileOnly(project(":module:basic:basic-configuration"))
}

tasks {
    withType<ShadowJar> {
        relocate("redis.clients.jedis.", "redis.clients.jedis_4_2_3.")
        relocate("com.electronwill.nightconfig.core.conversion", "taboolib.library.configuration")
        relocate("com.electronwill.nightconfig.", "com.electronwill.nightconfig_3_6_7.")
        relocate("org.apache.commons.pool2.", "org.apache.commons.pool2_2_11_1.")
        relocate("org.yaml.snakeyaml.", "org.yaml.snakeyaml_2_0.")
    }
}
