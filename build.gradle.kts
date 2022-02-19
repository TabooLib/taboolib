plugins {
    `maven-publish`
    id("org.jetbrains.kotlin.jvm") version "1.5.10" apply false
    id("com.github.johnrengelman.shadow") version "7.1.2" apply false
}

subprojects {
    apply(plugin = "java-library")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "com.github.johnrengelman.shadow")

    repositories {
        maven("https://libraries.minecraft.net")
        maven("https://repo1.maven.org/maven2")
        maven("https://maven.aliyun.com/repository/central")
        maven("https://repo.codemc.io/repository/nms/")
        maven("https://repo.tabooproject.org/repository/releases")
        mavenCentral()
    }
    dependencies {
        "compileOnly"(kotlin("stdlib"))
    }
    tasks.withType<Jar> {
        destinationDirectory.set(file("$rootDir/build/libs"))
    }
    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
    configure<JavaPluginConvention> {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
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
            artifactId = "taboolib"
            groupId = "io.izzel"
            version = (if (project.hasProperty("build")) "${project.version}-${project.findProperty("build")}" else "${project.version}")
            println("> version $version")
            file("$buildDir/libs").listFiles()?.forEach { file ->
                if (file.extension == "jar") {
                    artifact(file) {
                        classifier = file.nameWithoutExtension.substring(0, file.nameWithoutExtension.length - project.version.toString().length - 1)
                        println("> module $classifier (${file.name})")
                    }
                }
            }
        }
    }
}