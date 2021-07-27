repositories {
    maven { url = uri("https://nexus.velocitypowered.com/repository/maven-public/") }
}

dependencies {
    compileOnly("com.velocitypowered:velocity-api:1.1.8")
    compileOnly(project(":common"))
}