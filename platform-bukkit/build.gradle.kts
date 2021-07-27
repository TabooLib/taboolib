repositories {
    maven { url = uri("https://jitpack.io") }
    maven { url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/" )}
}

dependencies {
    compileOnly("net.milkbowl.vault:Vault:1:all")
    compileOnly("me.clip:placeholderapi:2.10.10")
    compileOnly("ink.ptms.core:v11600:11600:all")
    compileOnly("ink.ptms.core:v11200:11200:all")
    compileOnly(project(":common"))
    compileOnly(project(":common-5"))
    compileOnly(project(":module-chat"))
}