import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("com.github.johnrengelman.shadow") version "7.0.0"
    java
    kotlin("jvm") version "1.5.10"
}

repositories {
    maven {
        url = uri("https://repo1.maven.org/maven2")
    }
    maven {
        isAllowInsecureProtocol = true
        url = uri("http://repo.ptms.ink/repository/maven-releases/")
    }
    mavenCentral()
}

dependencies {
    implementation("io.izzel.kether:common:1.0.12")
    compileOnly("com.google.guava:guava:17.0")
    compileOnly("org.apache.commons:commons-lang3:3.5")
    compileOnly(project(":common"))
    compileOnly(project(":common-5"))
    compileOnly(project(":module-dependency"))
    compileOnly(project(":module-configuration"))
    compileOnly(kotlin("stdlib"))
}

tasks {
    named<ShadowJar>("shadowJar") {
        archiveClassifier.set("")
        dependencies {
            include(dependency("io.izzel.kether:common:1.0.12"))
        }
        exclude("io/izzel/kether/common/util/Coerce.class")
        exclude("LICENSE")
        exclude("LICENSE-Coerce")
        relocate("LICENSE", "LICENSE-Kether")
        relocate("io.izzel.kether.common.api.data", "taboolib.library.kether")
        relocate("io.izzel.kether.common.api", "taboolib.library.kether")
        relocate("io.izzel.kether.common.util", "taboolib.library.kether")
        relocate("io.izzel.kether.common.loader.types", "taboolib.library.kether")
        relocate("io.izzel.kether.common.loader", "taboolib.library.kether")
        relocate("io.izzel.kether.common.actions", "taboolib.library.kether.actions")
    }
    build {
        dependsOn(shadowJar)
    }
}