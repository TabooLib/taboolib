repositories {
    maven { url = uri("https://jitpack.io") }
    maven { url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/" )}
}

dependencies {
    compileOnly("net.milkbowl.vault:Vault:1:all")
    compileOnly("me.clip:placeholderapi:2.10.10")
//    compileOnly("org.spigotmc:spigot:1.17.1-R0.1-20210822.084823-7")
//    compileOnly("org.spigotmc:spigot:1.16.5-R0.1-20210611.090701-17")
//    compileOnly("org.spigotmc:spigot:1.12.2-R0.1-20190527.155359-34")
//    compileOnly("org.spigotmc:spigot:1.14.4-R0.1-20191224.232152-15")
    compileOnly("ink.ptms.core:v11701:11701:universal")
    compileOnly("ink.ptms.core:v11600:11600:all")
    compileOnly("ink.ptms.core:v11200:11200:all")
    compileOnly("ink.ptms.core:v11400:11400:all")
    compileOnly(project(":common"))
    compileOnly(project(":common-5"))
    compileOnly(project(":module-chat"))
    compileOnly(project(":module-lang"))
    compileOnly(project(":module-configuration"))
}