package taboolib.module.nms

import org.bukkit.entity.Player
import taboolib.common.io.runningClasses
import org.tabooproject.reflex.Reflex.Companion.getProperty
import org.tabooproject.reflex.Reflex.Companion.invokeMethod
import java.util.concurrent.ConcurrentHashMap

val nmsProxyMap = ConcurrentHashMap<String, Any>()

fun obcClass(name: String): Class<*> {
    return Class.forName("org.bukkit.craftbukkit.${MinecraftVersion.minecraftVersion}.$name")
}

fun nmsClass(name: String): Class<*> {
    return if (MinecraftVersion.isUniversal) {
        Class.forName(MinecraftVersion.mapping.classMap[name]!!.replace('/', '.'))
    } else {
        Class.forName("net.minecraft.server.${MinecraftVersion.minecraftVersion}.$name")
    }
}

@Suppress("UNCHECKED_CAST")
fun <T> nmsProxy(clazz: Class<T>, bind: String = "{name}Impl"): T {
    return nmsProxyMap.computeIfAbsent("${clazz.name}:$bind") {
        val bindClass = bind.replace("{name}", clazz.name)
        val instance = AsmClassTransfer(bindClass).run().getDeclaredConstructor().newInstance()
        runningClasses.forEach {
            if (it.name.startsWith("$bindClass\$")) {
                AsmClassTransfer(it.name).run()
            }
        }
        instance
    } as T
}

inline fun <reified T> nmsProxy(bind: String = "{name}Impl"): T {
    return nmsProxy(T::class.java, bind)
}

/**
 * 向玩家发送数据包
 */
fun Player.sendPacket(packet: Any) {
    // 1.18
    if (MinecraftVersion.major >= 10) {
        getProperty<Any>("entity/connection")!!.invokeMethod<Any>("send", packet)
    } else if (MinecraftVersion.isUniversal) {
        getProperty<Any>("entity/connection")!!.invokeMethod<Any>("sendPacket", packet)
    } else {
        getProperty<Any>("entity/playerConnection")!!.invokeMethod<Any>("sendPacket", packet)
    }
}