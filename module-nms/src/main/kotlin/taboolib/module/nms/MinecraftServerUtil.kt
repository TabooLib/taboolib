package taboolib.module.nms

import org.bukkit.entity.Player
import taboolib.common.io.classes
import taboolib.common.reflect.Reflex.Companion.reflex
import taboolib.common.reflect.Reflex.Companion.reflexInvoke
import java.util.concurrent.ConcurrentHashMap

val nmsProxyMap = ConcurrentHashMap<String, Any>()

fun obcClass(name: String): Class<*> {
    return Class.forName("org.bukkit.craftbukkit.${MinecraftVersion.legacyVersion}.$name")
}

fun nmsClass(name: String): Class<*> {
    return if (MinecraftVersion.isUniversal) {
        Class.forName(MinecraftVersion.mapping?.classMap?.get(name).toString())
    } else {
        Class.forName("net.minecraft.server.${MinecraftVersion.legacyVersion}.$name")
    }
}

@Suppress("UNCHECKED_CAST")
fun <T> nmsProxy(clazz: Class<T>, bind: String = "{name}Impl"): T {
    return nmsProxyMap.computeIfAbsent("${clazz.name}:$bind") {
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

/**
 * 向玩家发送数据包
 */
fun Player.sendPacket(packet: Any) {
    reflexInvoke<Any>("getHandle")!!.reflex<Any>("playerConnection")!!.reflexInvoke<Any>("sendPacket", packet)
}