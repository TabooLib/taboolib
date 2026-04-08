repositories {
    maven("https://libraries.minecraft.net")
}

dependencies {
    compileOnly(project(":module:bukkit-nms"))
    compileOnly(project(":module:bukkit-nms:bukkit-nms-tag"))
    compileOnly("ink.ptms.core:v12104:12104:mapped")
    compileOnly("ink.ptms.core:v260100:260100-minimize")
}