repositories {
    maven("https://repo.minebench.de/")
    maven("https://www.jitpack.io")
}

dependencies {
    compileOnly("cn.powernukkitx:powernukkitx:1.20.0-r2")
    compileOnly("org.tabooproject.reflex:reflex:1.0.19")
    compileOnly("org.tabooproject.reflex:analyser:1.0.19")
    compileOnly(project(":common"))
}