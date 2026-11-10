dependencies {
    compileOnly(project(":common"))
    compileOnly(project(":common-util"))
    compileOnly(project(":common-platform-api"))
    compileOnly("com.hypixel:hytale-server:1.0.0")

    testImplementation(project(":common"))
    testImplementation(project(":common-util"))
    testImplementation(project(":common-platform-api"))
    testImplementation("com.hypixel:hytale-server:1.0.0")
}

tasks.test {
    systemProperty("java.util.logging.manager", "com.hypixel.hytale.logger.backend.HytaleLogManager")
}
