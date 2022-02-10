dependencies {
    compileOnly("com.google.code.gson:gson:2.8.9")
    compileOnly("com.google.guava:guava:21.0")
    compileOnly("org.apache.commons:commons-lang3:3.5")
    compileOnly(project(":common-core"))
    compileOnly(project(":common-environment"))
    // Mirror
    compileOnly(project(":common-plugin"))
    compileOnly(project(":common-adapter"))
}