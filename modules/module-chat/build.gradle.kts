repositories {
    maven { url = uri("https://repo.codemc.io/repository/maven-public/") }
}

dependencies {
    compileOnly("net.md-5:bungeecord-chat:1.17-R0.1-SNAPSHOT")
    compileOnly(project(":common"))
}