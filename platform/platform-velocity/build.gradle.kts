repositories {
    maven { url = uri("https://repo.papermc.io/repository/maven-public/") }
}

dependencies {
    compileOnly(project(":common"))
    compileOnly(project(":common-platform-api"))
    compileOnly("com.velocitypowered:velocity-api:3.1.1")
}