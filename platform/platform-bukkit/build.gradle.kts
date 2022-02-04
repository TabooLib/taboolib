tasks {
    withType<Jar> {
        destinationDirectory.set(file("project/platform/bukkit"))
    }
}