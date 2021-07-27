repositories {
    maven { url = uri("https://nexus.velocitypowered.com/repository/maven-public/") }
}

dependencies {
    compileOnly("com.google.guava:guava:21.0")
    compileOnly("ink.ptms.core:v11605:11605")
    compileOnly("net.md_5.bungee:BungeeCord:1:all")
    compileOnly("com.velocitypowered:velocity-api:1.1.8")
    compileOnly(project(":common"))
}