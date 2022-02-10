import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.tabooproject.shrinkingkt.ShrinkingExt

plugins {
    `maven-publish`
    id("org.jetbrains.kotlin.jvm") version "1.5.10" apply false
    id("org.tabooproject.shrinkingkt") version "1.0.4" apply false
    id("com.github.johnrengelman.shadow") version "7.1.2" apply false
}

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.tabooproject.shrinkingkt")
    apply(plugin = "com.github.johnrengelman.shadow")

    repositories {
        maven("https://repo.tabooproject.org/repository/releases/")
        mavenCentral()
    }

    dependencies {
        "compileOnly"(kotlin("stdlib"))
        "compileOnly"("org.tabooproject.reflex:analyser:1.0.5")
        "compileOnly"("org.tabooproject.reflex:reflex:1.0.5")
        "testRuntimeOnly"("org.junit.jupiter:junit-jupiter-engine:5.8.1")
        "testImplementation"("org.junit.jupiter:junit-jupiter-api:5.8.1")
    }

    configure<JavaPluginExtension> {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    configure<ShrinkingExt> {
        annotation = "taboolib.internal.Internal"
    }

    publishing {
        repositories {
            maven("http://ptms.ink:8081/repository/releases") {
                isAllowInsecureProtocol = true
                credentials {
                    username = project.findProperty("taboolibUsername").toString()
                    password = project.findProperty("taboolibPassword").toString()
                }
                authentication {
                    create<BasicAuthentication>("basic")
                }
            }
        }
        publications {
            create<MavenPublication>("maven") {
                from(components["java"])
            }
        }
    }

    tasks {
        withType<Test> {
            useJUnitPlatform()
        }

        withType<ShadowJar> {
            archiveClassifier.set("")
            relocate("org.tabooproject.reflex", "taboolib.common.reflect")
        }

        withType<JavaCompile> {
            options.encoding = "UTF-8"
            options.compilerArgs.addAll(listOf("-XDenableSunApiLintControl"))
        }
    }
}