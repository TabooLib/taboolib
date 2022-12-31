import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `maven-publish`
    java
    id("org.jetbrains.kotlin.jvm") version "1.5.10" apply false
    id("com.github.johnrengelman.shadow") version "7.1.2" apply false
}

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "com.github.johnrengelman.shadow")
    apply(plugin = "maven-publish")

    repositories {
        maven("https://libraries.minecraft.net")
        maven("https://repo1.maven.org/maven2")
        maven("https://maven.aliyun.com/repository/central")
        maven("https://repo.codemc.io/repository/nms/")
        maven("http://ptms.ink:8081/repository/releases") {
            isAllowInsecureProtocol = true
        }
        mavenLocal()
        mavenCentral()
    }

    dependencies {
        compileOnly(kotlin("stdlib"))
    }

    java {
         // withJavadocJar()
         withSourcesJar()
    }

    tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
        archiveClassifier.set("")
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.compilerArgs.addAll(listOf("-XDenableSunApiLintControl"))
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
            freeCompilerArgs = listOf("-Xjvm-default=all")
        }
    }

    configure<JavaPluginConvention> {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

subprojects
    .filter { it.name != "module" && it.name != "platform" && it.name != "expansion" }
    .filter { it.name != "module-database-core" }
    .forEach { proj ->
        proj.publishing { applyToSub(proj) }
    }

fun PublishingExtension.applyToSub(subProject: Project) {
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
        mavenLocal()
    }
    publications {
        create<MavenPublication>("maven") {
            artifactId = subProject.name
            groupId = "io.izzel.taboolib"
            version = (if (project.hasProperty("build")) {
                var build = project.findProperty("build").toString()
                if (build.startsWith("task ")) {
                    build = "local"
                }
                "${project.version}-$build"
            } else {
                "${project.version}"
            })
            artifact(subProject.tasks["kotlinSourcesJar"])
            artifact(subProject.tasks["shadowJar"])
            println("> Apply \"$groupId:$artifactId:$version\"")
        }
    }
}