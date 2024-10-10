import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

dependencies {
    compileOnly("redis.clients:jedis:4.2.3")
    compileOnly(project(":common"))
    compileOnly(project(":common-env"))
    compileOnly(project(":common-util"))
    compileOnly(project(":common-platform-api"))
    compileOnly(project(":module:basic:basic-configuration"))
    compileOnly(project(":module:database:database-alkaid-redis"))
    compileOnly("com.fasterxml.jackson.core:jackson-databind:2.17.2")
    compileOnly("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.2")
}

tasks {
    withType<ShadowJar> {
        relocate("redis.clients.jedis.", "redis.clients.jedis_4_2_3.")
        relocate("com.electronwill.nightconfig.core.conversion", "taboolib.library.configuration")
        relocate("org.yaml.snakeyaml.", "org.yaml.snakeyaml_2_0.")
    }
}
