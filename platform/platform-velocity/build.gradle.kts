repositories {
    maven { url = uri("https://nexus.velocitypowered.com/repository/maven-public/") }
}

dependencies {
    compileOnly("com.velocitypowered:velocity-api:3.0.1")
    compileOnly(project(":common"))
}