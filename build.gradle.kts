plugins {
    `maven-publish`
}

allprojects {
    group = "taboolib"
    version = "test-600-0"

    tasks.withType<Jar> {
        destinationDirectory.set(file("$rootDir/build/libs"))
    }
    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
}

publishing {
    repositories {
        maven {
            url = uri("https://repo2s.ptms.ink/repository/maven-releases/")
            credentials {
                username = project.findProperty("user").toString()
                password = project.findProperty("password").toString()
            }
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
    publications {
        create<MavenPublication>("maven") {
            groupId = "io.izzel"
            artifactId = "taboolib"
            version = project.version.toString()
            // subprojects -> classifier
            file("$buildDir/libs").listFiles()?.forEach { file ->
                artifact(file) {
                    classifier = file.nameWithoutExtension.substring(0, file.nameWithoutExtension.length - version.length - 1)
                }
            }
        }
    }
}