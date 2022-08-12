package taboolib.module.nms

import org.bukkit.entity.Player
import taboolib.common.io.runningClassMap
import taboolib.common.io.runningClasses
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

val nmsProxyMap = ConcurrentHashMap<String, Any>()

private val packetPool = Executors.newFixedThreadPool(32)

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
        runningClassMap.forEach { (name, _) ->
            if (name.startsWith("$bindClass\$")) {
                AsmClassTransfer(name).run()
            }
        }
        instance
    } as T
}

inline fun <reified T> nmsProxy(bind: String = "{name}Impl"): T {
    return nmsProxy(T::class.java, bind)
}

/**
 * 向玩家发送数据包（异步）
 */
fun Player.sendPacket(packet: Any): CompletableFuture<Unit> {
    val future = CompletableFuture<Unit>()
    packetPool.submit {
        try {
            future.complete(sendPacketBlocking(packet))
        } catch (e: Throwable) {
            future.completeExceptionally(e)
            e.printStackTrace()
        }
    }
    return future
}

/**
 * 向玩家发送数据包
 */
fun Player.sendPacketBlocking(packet: Any) {
    PacketSender.sendPacket(this, packet)
}