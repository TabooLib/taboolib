dependencies {
    compileOnly(project(":common"))
    compileOnly(project(":common-util"))
    compileOnly(project(":common-platform-api"))
    compileOnly(project(":module:database"))
    compileOnly(project(":module:basic:basic-configuration"))
    compileOnly("ink.ptms.core:v11701:11701-minimize:universal")

    testImplementation(project(":common"))
    testImplementation(project(":common-util"))
    testImplementation(project(":common-platform-api"))
    testImplementation(project(":module:database"))
    testImplementation(project(":module:basic:basic-configuration"))
    testImplementation("com.zaxxer:HikariCP:4.0.3")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testImplementation("org.xerial:sqlite-jdbc:3.42.0.0")
}