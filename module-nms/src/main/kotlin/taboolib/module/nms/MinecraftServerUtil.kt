package taboolib.module.nms

import taboolib.common.io.classes
import java.util.concurrent.ConcurrentHashMap

private val nmsProxyCacheMap = ConcurrentHashMap<String, Any>()

fun obcClass(name: String): Class<*> {
    return Class.forName("org.bukkit.craftbukkit." + MinecraftVersion.legacyVersion + "." + name)
}

fun nmsClass(name: String) {
    if (MinecraftVersion.isUniversal) {
        Class.forName(MinecraftVersion.mapping?.classMap?.get(name).toString())
    } else {
        Class.forName("net.minecraft.server." + MinecraftVersion.legacyVersion + "." + name)
    }
}

@Suppress("UNCHECKED_CAST")
fun <T> nmsProxy(clazz: Class<T>, bind: String): T {
    return nmsProxyCacheMap.computeIfAbsent("${clazz.name}:$bind") {
        val bindClass = bind.replace("{name}", clazz.name)
        val instance = AsmClassTransfer(bindClass).run().getDeclaredConstructor().newInstance()
        classes.forEach {
            if (it.name.startsWith("$bindClass\$")) {
                AsmClassTransfer(it.name).run()
            }
        }
        instance
    } as T
}