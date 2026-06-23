dependencies {
    compileOnly(project(":common"))
    compileOnly(project(":common-util"))
    compileOnly(project(":common-legacy-api"))
    compileOnly(project(":common-platform-api"))
    compileOnly(project(":module:database"))
    compileOnly(project(":module:basic:basic-configuration"))
    compileOnly("ink.ptms.core:v11701:11701-minimize:universal")
    testImplementation(project(":common"))
    testImplementation(project(":common-util"))
    testImplementation(project(":common-legacy-api"))
    testImplementation(project(":common-platform-api"))
    testImplementation(project(":module:database"))
    testImplementation(project(":module:basic:basic-configuration"))
    testImplementation("com.zaxxer:HikariCP:4.0.3")
    testImplementation("org.xerial:sqlite-jdbc:3.42.0.0")
}

// Gradle 8.9：测试类路径会消费依赖工程的 shadowJar 产物，必须显式声明任务依赖
tasks.withType<Test> {
    dependsOn(
        project(":common").tasks.named("shadowJar"),
        project(":common-util").tasks.named("shadowJar"),
        project(":common-legacy-api").tasks.named("shadowJar"),
        project(":common-platform-api").tasks.named("shadowJar"),
        project(":module:database").tasks.named("shadowJar"),
        project(":module:basic:basic-configuration").tasks.named("shadowJar"),
    )
}
