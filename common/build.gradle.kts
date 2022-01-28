tasks {
    withType<Jar> {
        destinationDirectory.set(file("build/libs"))
    }
}