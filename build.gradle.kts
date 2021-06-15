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