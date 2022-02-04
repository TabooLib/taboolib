repositories {
    maven { url = uri("https://repo.spongepowered.org/maven") }
}

dependencies {
    compileOnly("org.spongepowered:spongeapi:7.2.0")
    compileOnly(project(":common:common-core"))
}