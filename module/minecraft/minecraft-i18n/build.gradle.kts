dependencies {
    compileOnly(project(":common"))
    compileOnly(project(":common-legacy-api"))
    compileOnly(project(":common-env"))
    compileOnly(project(":common-platform-api"))
    compileOnly(project(":common-util"))
    compileOnly(project(":module:minecraft:minecraft-chat"))
    compileOnly(project(":module:basic:basic-configuration"))
    testImplementation(project(":common-platform-api"))
    testImplementation(project(":common-util"))
    testImplementation(project(":module:minecraft:minecraft-chat"))
    testImplementation(project(":module:basic:basic-configuration"))
}