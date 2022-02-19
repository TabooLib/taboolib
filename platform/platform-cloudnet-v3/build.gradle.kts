repositories {
    maven { url = uri("https://repo.cloudnetservice.eu/repository/snapshots/") }
    maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots/") }
}

dependencies {
    compileOnly("de.dytanic.cloudnet:cloudnet:3.5.0-SNAPSHOT")
    compileOnly("de.dytanic.cloudnet:cloudnet-bridge:3.5.0-SNAPSHOT")
    compileOnly("net.kyori:adventure-api:4.8.1") // 参照 CloudNet
    compileOnly("net.kyori:adventure-text-serializer-gson:4.8.1") // 参照 CloudNet
    compileOnly("net.kyori:adventure-text-serializer-legacy:4.8.1") // 参照 CloudNet
    compileOnly(project(":common"))
}