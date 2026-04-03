dependencies {
    compileOnly(project(":common"))
    compileOnly(project(":common-util"))
    compileOnly(project(":common-platform-api"))
    compileOnly(project(":module:minecraft:minecraft-chat"))
    compileOnly(project(":module:minecraft:minecraft-i18n"))
    testImplementation(project(":common"))
    testImplementation(project(":common-util"))
    testImplementation(project(":common-platform-api"))
}

tasks.withType<Test> {
    dependsOn(
        project(":common").tasks.named("shadowJar"),
        project(":common-util").tasks.named("shadowJar"),
        project(":common-platform-api").tasks.named("shadowJar")
    )
}