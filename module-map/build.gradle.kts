repositories {
    mavenCentral()
}

dependencies {
    compileOnly("ink.ptms.core:v11701:11701:mapped")
    compileOnly("ink.ptms.core:v11701:11701:universal")
    compileOnly(project(":platform-bukkit"))
    compileOnly(kotlin("stdlib"))
}
