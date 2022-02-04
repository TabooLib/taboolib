repositories {
    maven { url = uri("https://repo.spongepowered.org/maven") }
}

dependencies {
    compileOnly("org.spongepowered:spongeapi:9.0.0-SNAPSHOT")
    compileOnly(project(":common:common-core"))
}

java {
    sourceCompatibility = JavaVersion.VERSION_16
    targetCompatibility = JavaVersion.VERSION_16
}