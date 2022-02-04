tasks {
    withType<Jar> {
        destinationDirectory.set(file("project/common"))
    }
}