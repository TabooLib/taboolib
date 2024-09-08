import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

dependencies {
    compileOnly(project(":common"))
    compileOnly(project(":common-legacy-api"))
    compileOnly(project(":common-env"))
    compileOnly(project(":common-platform-api"))
    compileOnly(project(":common-util"))
    // 用于支持颜色转换
    compileOnly(project(":module:minecraft:minecraft-chat"))
    // 基本库
    compileOnly("org.yaml:snakeyaml:2.2")
    compileOnly("com.typesafe:config:1.4.3")
    compileOnly("com.electronwill.night-config:core:3.6.7")
    compileOnly("com.electronwill.night-config:toml:3.6.7")
    compileOnly("com.electronwill.night-config:json:3.6.7")
    compileOnly("com.electronwill.night-config:hocon:3.6.7")
    implementation("com.electronwill.night-config:core-conversion:6.0.0")
}
repositories {
    mavenCentral()
}

tasks {
    withType<ShadowJar> {
        dependencies {
            include(dependency("com.electronwill.night-config:core-conversion:6.0.0"))
        }
        // nightconfig
        relocate("com.electronwill.nightconfig.core.conversion", "taboolib.library.configuration")
        relocate("com.electronwill.nightconfig", "com.electronwill.nightconfig_3_6_7")
        // snakeyaml
        relocate("org.yaml.snakeyaml", "org.yaml.snakeyaml_2_2")
    }
}