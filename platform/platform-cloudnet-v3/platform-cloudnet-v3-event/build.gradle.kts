dependencies {
    compileOnly("de.dytanic.cloudnet:cloudnet:3.5.0-SNAPSHOT")
    compileOnly("de.dytanic.cloudnet:cloudnet-bridge:3.5.0-SNAPSHOT")
    compileOnly("net.kyori:adventure-api:4.9.3") // 参照 CloudNet
    compileOnly("net.kyori:adventure-text-serializer-gson:4.9.3") // 参照 CloudNet
    compileOnly("net.kyori:adventure-text-serializer-legacy:4.9.3") // 参照 CloudNet
    compileOnly(project(":common:common-core"))
    compileOnly(project(":common:common-event"))
    compileOnly(project(":platform:platform-cloudnet-v3:platform-cloudnet-v3-core"))
}