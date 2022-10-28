import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

dependencies {
    compileOnly("redis.clients:jedis:4.2.3")
    compileOnly(project(":common"))
    compileOnly(project(":module:module-configuration"))
}

tasks {
    withType<ShadowJar> {
        archiveClassifier.set("")
        relocate("redis.clients.jedis.", "redis.clients.jedis_4_2_3.")
        relocate("org.tabooproject", "taboolib.library")
    }
    build {
        dependsOn(shadowJar)
    }
}