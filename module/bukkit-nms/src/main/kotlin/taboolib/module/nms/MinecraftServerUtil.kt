@file:Suppress("UNCHECKED_CAST")

package taboolib.module.nms

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.tabooproject.reflex.Reflex.Companion.getProperty
import taboolib.common.Inject
import taboolib.common.io.runningClassMapWithoutLibrary
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.reflect.ClassHelper
import taboolib.common.util.unsafeLazy
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

private val nmsProxyClassMap = ConcurrentHashMap<String, Class<*>>()

private val nmsProxyInstanceMap = ConcurrentHashMap<String, Any>()

private val packetPool = ConcurrentHashMap<String, ExecutorService>()

/**
 * 服务器是否在运行
 */
val isBukkitServerRunning: Boolean
    get() {
        return try {
            !Bukkit.getServer().getProperty<Boolean>("console/stopped")!!
        } catch (ex: NoSuchFieldException) {
            !Bukkit.getServer().getProperty<Boolean>("console/hasStopped")!!
        }
    }

/**
 * 获取 MinecraftServer 实例
 */
val minecraftServerObject: Any by unsafeLazy {
    Bukkit.getServer().getProperty("console")!!
}

/**
 * 获取 OBC 类
 */
fun obcClass(name: String): Class<*> {
    return if (MinecraftVersion.isUniversalCraftBukkit) {
        ClassHelper.getClass("org.bukkit.craftbukkit.$name")
    } else {
        ClassHelper.getClass("org.bukkit.craftbukkit.${MinecraftVersion.minecraftVersion}.$name")
    }
}

/**
 * 获取 NMS 类
 */
fun nmsClass(name: String): Class<*> {
    return if (MinecraftVersion.isUniversal) {
        ClassHelper.getClass(MinecraftVersion.spigotMapping.classMapSpigotS2F[name]?.replace('/', '.') ?: throw ClassNotFoundException(name))
    } else {
        ClassHelper.getClass("net.minecraft.server.${MinecraftVersion.minecraftVersion}.$name")
    }
}

@Synchronized
fun <T> nmsProxy(clazz: Class<T>, bind: String = "{name}Impl", vararg parameter: Any): T {
    return nmsProxy(clazz, bind, emptyList(), *parameter)
}

@Synchronized
fun <T> nmsProxy(clazz: Class<T>, bind: String = "{name}Impl", parent: List<String> = emptyList(), vararg parameter: Any): T {
    val key = "${clazz.name}:$bind:${parameter.joinToString(",") { it.javaClass.name.toString() }}"
    // 从缓存中获取
    if (nmsProxyInstanceMap.containsKey(key)) {
        return nmsProxyInstanceMap[key] as T
    }
    // 获取合适的构造函数并创建实例
    fun <T> createInstance(clazz: Class<T>, parameters: Array<out Any>): T {
        // 遍历所有构造函数
        val constructors = clazz.declaredConstructors
        for (constructor in constructors) {
            val parameterTypes = constructor.parameterTypes
            if (parameterTypes.size != parameters.size) continue
            var isMatch = true
            for (i in parameterTypes.indices) {
                if (!parameterTypes[i].isAssignableFrom(parameters[i].javaClass)) {
                    isMatch = false
                    break
                }
            }
            if (isMatch) {
                constructor.isAccessible = true
                return constructor.newInstance(*parameters) as T
            }
        }
        throw NoSuchMethodException("没有找到匹配的构造函数: ${clazz.name}")
    }
    // 获取代理类并实例化
    val newInstance = createInstance(nmsProxyClass(clazz, bind, parent), parameter)
    // 缓存实例
    nmsProxyInstanceMap[key] = newInstance!!
    return newInstance
}

inline fun <reified T> nmsProxy(bind: String = "{name}Impl", vararg parameter: Any): T {
    return nmsProxy(T::class.java, bind, emptyList(), *parameter)
}

inline fun <reified T> nmsProxy(bind: String = "{name}Impl", parent: List<String> = emptyList(), vararg parameter: Any): T {
    return nmsProxy(T::class.java, bind, parent, *parameter)
}

@Synchronized
fun <T> nmsProxyClass(clazz: Class<T>, bind: String = "{name}Impl", parent: List<String> = emptyList()): Class<T> {
    parent.forEach { nmsProxyClass(clazz, it) }
    return nmsProxyClass(clazz, bind)
}

@Synchronized
fun <T> nmsProxyClass(clazz: Class<T>, bind: String = "{name}Impl"): Class<T> {
    val key = "${clazz.name}:$bind"
    // 从缓存中获取
    if (nmsProxyClassMap.containsKey(key)) {
        return nmsProxyClassMap[key] as Class<T>
    }
    // 生成代理类
    val bindClass = bind.replace("{name}", clazz.name)
    val newClass = AsmClassTranslation(bindClass).createNewClass()
    // 同时生成所有的内部类
    runningClassMapWithoutLibrary.filter { (name, _) -> name.startsWith("$bindClass\$") }.forEach { (name, _) ->
        nmsProxyClassMap["$name:$bind"] = AsmClassTranslation(name).createNewClass()
    }
    // 缓存代理类
    nmsProxyClassMap[key] = newClass
    return newClass as Class<T>
}

inline fun <reified T> nmsProxyClass(bind: String = "{name}Impl", parent: List<String> = emptyList()): Class<T> {
    return nmsProxyClass(T::class.java, bind, parent)
}

/**
 * 向玩家发送打包数据包（异步，1.19.4+）
 */
fun Player.sendBundlePacket(vararg packet: Any): CompletableFuture<Void> {
    return sendBundlePacket(packet.toList())
}

/**
 * 向玩家发送打包数据包（异步，1.19.4+）
 */
fun Player.sendBundlePacket(packet: List<Any>): CompletableFuture<Void> {
    if (MinecraftVersion.isBundlePacketSupported) {
        val bundlePacket = PacketSender.createBundlePacket(packet)
        if (bundlePacket != null) {
            return sendPacket(bundlePacket)
        }
    }
    return CompletableFuture.allOf(*packet.map { sendPacket(it) }.toTypedArray())
}

/**
 * 向玩家发送数据包（异步）
 */
fun Player.sendPacket(packet: Any): CompletableFuture<Void> {
    val future = CompletableFuture<Void>()
    val pool = packetPool.computeIfAbsent(name) { Executors.newSingleThreadExecutor() }
    pool.submit {
        try {
            sendPacketBlocking(packet)
            future.complete(null)
        } catch (e: Throwable) {
            future.completeExceptionally(e)
            e.printStackTrace()
        }
    }
    return future
}

/**
 * 向玩家发送打包数据包（1.19.4+）
 */
fun Player.sendBundlePacketBlocking(vararg packet: Any) {
    sendBundlePacketBlocking(packet.toList())
}

/**
 * 向玩家发送打包数据包（1.19.4+）
 */
fun Player.sendBundlePacketBlocking(packet: List<Any>) {
    if (MinecraftVersion.isBundlePacketSupported) {
        val bundlePacket = PacketSender.createBundlePacket(packet)
        if (bundlePacket != null) {
            sendPacketBlocking(bundlePacket)
            return
        }
    }
    packet.forEach { sendPacketBlocking(it) }
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
@Inject
@PlatformSide(Platform.BUKKIT)
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