repositories {
    maven("https://repo.spongepowered.org/maven")
}

dependencies {
    compileOnly("org.spongepowered:spongeapi:9.0.0-SNAPSHOT")
    compileOnly(project(":common"))
}