allprojects {
    group = "taboolib"
    version = "6.0.0"

    tasks.withType(Jar::class.java) {
        destinationDirectory.set(file("$rootDir/build/libs"))
    }

    tasks.withType(JavaCompile::class.java) {
        options.encoding = "UTF-8"
    }
}

tasks.withType(Delete::class.java) {
    fileTree("$rootDir/build").forEach { it.delete() }
}