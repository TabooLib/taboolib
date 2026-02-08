dependencies {
    compileOnly(project(":common"))
    compileOnly(project(":common-env"))
    compileOnly(project(":common-platform-api"))
    compileOnly(project(":common-util"))
    testImplementation(project(":common"))
    testImplementation(project(":common-platform-api"))
    testImplementation(project(":common-util"))
}