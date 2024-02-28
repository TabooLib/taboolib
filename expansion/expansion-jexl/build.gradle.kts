import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

dependencies {
    compileOnly(project(":common"))
    compileOnly(project(":common-util"))
    compileOnly(project(":common-env"))
    // 表达式
    compileOnly("org.apache.commons:commons-jexl3:3.2.1")
}

tasks {
    withType<ShadowJar> {
        relocate("org.apache.commons.jexl3", "org.apache.commons.jexl3_3_2_1")
    }
}