package taboolib.module.nms

fun nmsClass(name: String) {
    if (MinecraftVersion.isUniversal) {
        Class.forName(MinecraftVersion.mapping?.classMap?.get(name).toString())
    } else {
        Class.forName("net.minecraft.server." + MinecraftVersion.legacyVersion + "." + name)
    }
}

fun obcClass(name: String): Class<*> {
    return Class.forName("org.bukkit.craftbukkit." + MinecraftVersion.legacyVersion + "." + name)
}