repositories {
    maven { url = uri("https://repo.spongepowered.org/maven") }
}

dependencies {
    compileOnly("org.spongepowered:spongeapi:8.0.0-SNAPSHOT")
    compileOnly(project(":common:common-core"))
}