@file:Suppress("GradlePackageUpdate")

dependencies {
    compileOnly("com.google.code.gson:gson:2.8.7")
    compileOnly(project(":common"))
    compileOnly(project(":module:module-configuration"))
    compileOnly("ink.ptms.core:v11701:11701-minimize:universal")
    compileOnly(project(":platform:platform-bukkit"))
}