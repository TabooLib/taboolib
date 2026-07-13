repositories {
    maven { url = uri("https://repo.papermc.io/repository/maven-public/") }
}

dependencies {
    compileOnly(project(":common"))
    compileOnly(project(":common-util"))
    compileOnly(project(":common-platform-api"))
    compileOnly(project(":platform:platform-velocity"))
    compileOnly("com.velocitypowered:velocity-api:3.1.1")

    testImplementation(project(":common"))
    testImplementation(project(":common-util"))
    testImplementation(project(":common-platform-api"))
    testImplementation(project(":platform:platform-velocity"))
    testImplementation("com.velocitypowered:velocity-api:3.1.1")
}