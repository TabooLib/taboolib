dependencies {
    testImplementation(project(":module:module-database-core"))
    testImplementation("ink.ptms.core:v11701:11701-minimize:universal")
    compileOnly(project(":common"))
    compileOnly(project(":common-5"))
    compileOnly(project(":module:module-database-core"))
    compileOnly(project(":module:module-configuration"))
    compileOnly("org.tabooproject.reflex:reflex:1.0.19")
    compileOnly("org.tabooproject.reflex:analyser:1.0.19")
    compileOnly("ink.ptms.core:v11701:11701-minimize:universal")
}
