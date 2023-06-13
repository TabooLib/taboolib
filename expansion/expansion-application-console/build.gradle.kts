dependencies {
    compileOnly(project(":common"))
    compileOnly(project(":platform:platform-application"))

    implementation("net.minecrell:terminalconsoleappender:1.3.0")
    implementation("org.apache.logging.log4j:log4j-api:2.17.2")
    implementation("org.apache.logging.log4j:log4j-core:2.17.2")
    implementation("org.apache.logging.log4j:log4j-iostreams:2.17.2")
    implementation("org.apache.logging.log4j:log4j-slf4j18-impl:2.17.2")
}