dependencies {
    compileOnly(project(":common"))
    compileOnly(project(":common-util"))
    compileOnly(project(":common-env"))
    compileOnly(project(":common-platform-api"))
    compileOnly("com.github.AfyerDev.AfyBroker:afybroker-server:f6261eab2a")
    compileOnly("org.slf4j:slf4j-api:1.7.32")

    testImplementation(project(":common"))
    testImplementation(project(":common-util"))
    testImplementation(project(":common-platform-api"))
    testImplementation("com.github.AfyerDev.AfyBroker:afybroker-server:f6261eab2a")
}