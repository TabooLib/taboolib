dependencies {
    compileOnly(project(":common:common-core"))
    compileOnly(project(":common:common-util")) // FileWatcher, Coerce
    compileOnly(project(":common:common-plugin"))
    compileOnly(project(":module:module-configuration:module-configuration-core"))
}