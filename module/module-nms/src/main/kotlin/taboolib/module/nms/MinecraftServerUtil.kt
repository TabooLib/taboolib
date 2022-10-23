package taboolib.module.nms

import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.tabooproject.reflex.Reflex.Companion.invokeConstructor
import taboolib.common.io.runningClassMapWithoutLibrary
import taboolib.common.platform.event.SubscribeEvent
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

private val nmsProxyClassMap = ConcurrentHashMap<String, Class<*>>()

private val nmsProxyInstanceMap = ConcurrentHashMap<String, Any>()

private val packetPool = ConcurrentHashMap<String, ExecutorService>()

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

/**
 * 禁用数据包监听器
 */
fun disablePacketListener() {
    ChannelExecutor.disable()
}

@Suppress("UNCHECKED_CAST")
@Synchronized
fun <T> nmsProxy(clazz: Class<T>, bind: String = "{name}Impl", vararg parameter: Any): T {
    val key = "${clazz.name}:$bind:${parameter.joinToString(",") { it.javaClass.name.toString() }}"
    // 从缓存中获取
    if (nmsProxyInstanceMap.containsKey(key)) {
        return nmsProxyInstanceMap[key] as T
    }
    // 获取代理类并实例化
    val newInstance = nmsProxyClass(clazz, bind).getDeclaredConstructor(*parameter.map { it.javaClass }.toTypedArray()).newInstance(*parameter)
    // 缓存实例
    nmsProxyInstanceMap[key] = newInstance!!
    return newInstance
}

inline fun <reified T> nmsProxy(bind: String = "{name}Impl", vararg parameter: Any): T {
    return nmsProxy(T::class.java, bind, *parameter)
}

@Suppress("UNCHECKED_CAST")
@Synchronized
fun <T> nmsProxyClass(clazz: Class<T>, bind: String = "{name}Impl"): Class<T> {
    val key = "${clazz.name}:$bind"
    // 从缓存中获取
    if (nmsProxyClassMap.containsKey(key)) {
        return nmsProxyClassMap[key] as Class<T>
    }
    // 生成代理类
    val bindClass = bind.replace("{name}", clazz.name)
    val newClass = AsmClassTransfer(bindClass).createNewClass()
    // 同时生成所有的内部类
    runningClassMapWithoutLibrary.filter { (name, _) -> name.startsWith("$bindClass\$") }.forEach { (name, _) -> AsmClassTransfer(name).createNewClass() }
    // 缓存代理类
    nmsProxyClassMap[key] = newClass
    return newClass as Class<T>
}

inline fun <reified T> nmsProxyClass(bind: String = "{name}Impl"): Class<T> {
    return nmsProxyClass(T::class.java, bind)
}

/**
 * 向玩家发送数据包（异步）
 */
fun Player.sendPacket(packet: Any): CompletableFuture<Unit> {
    val future = CompletableFuture<Unit>()
    val pool = packetPool.computeIfAbsent(name) { Executors.newSingleThreadExecutor() }
    pool.submit {
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

/**
 * 监听器
 */
private object PoolListener {

    @SubscribeEvent
    private fun onJoin(e: PlayerJoinEvent) {
        packetPool.computeIfAbsent(e.player.name) { Executors.newSingleThreadExecutor() }
    }

    @SubscribeEvent
    private fun onQuit(e: PlayerQuitEvent) {
        packetPool.remove(e.player.name)?.shutdownNow()
    }
}