@file:Suppress("GradlePackageUpdate")

dependencies {
    compileOnly(project(":common"))
    compileOnly(project(":common-env"))
    compileOnly(project(":common-util"))
    compileOnly("org.openjdk.nashorn:nashorn-core:15.4")
}