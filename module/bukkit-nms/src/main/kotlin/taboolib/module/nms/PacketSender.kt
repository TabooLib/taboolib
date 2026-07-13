package taboolib.module.nms

import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.tabooproject.reflex.ClassMethod
import org.tabooproject.reflex.Reflex.Companion.getProperty
import org.tabooproject.reflex.ReflexClass
import taboolib.common.Inject
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submitAsync
import taboolib.common.reflect.ClassHelper
import java.lang.reflect.Constructor
import java.util.concurrent.ConcurrentHashMap

/**
 * TabooLib
 * taboolib.module.nms.PacketSender
 *
 * @author 坏黑
 * @since 2022/7/19 16:02
 */
@Inject
@PlatformSide(Platform.BUKKIT)
object PacketSender {

    private val playerConnectionMap = ConcurrentHashMap<String, Any>()
    private var sendPacketMethod: Any? = null
    private var isFallbackMethod = false

    private var newPacketBundlePacket: Constructor<*>? = null
    private var useMinecraftMethod = false

    init {
        try {
            val bundlePacketClass = ClassHelper.getClass("net.minecraft.network.protocol.game.ClientboundBundlePacket")
            newPacketBundlePacket = bundlePacketClass.getDeclaredConstructor(Iterable::class.java)
            newPacketBundlePacket?.isAccessible = true
        } catch (ignored: Exception) {
        }
    }

    /**
     * 使用 Minecraft 方法发送数据包
     */
    @Deprecated("没啥用了")
    fun useMinecraftMethod() {
        useMinecraftMethod = true
    }

    /**
     * 创建混合包（我也不知道这东西应该翻译成什么）
     */
    fun createBundlePacket(packets: List<Any>): Any? {
        return newPacketBundlePacket?.newInstance(packets)
    }

    /**
     * 发送数据包
     * @param player 玩家
     * @param packet 数据包实例
     */
    fun sendPacket(player: Player, packet: Any) {
        // 使用原版方法发送数据包
        // 之前通过 TinyProtocol 的 channel.pipeline().writeAndFlush() 暴力发包会有概率出问题
        val connection = getConnection(player)
        if (sendPacketMethod == null) {
            // 在混合服务端下，直接使用原生反射避免触发类型加载
            if (MinecraftVersion.isCatServer) {
                sendPacketMethod = getSendPacketMethodHybrid(connection, packet)
                isFallbackMethod = true
            } else {
                try {
                    val reflexClass = ReflexClass.of(connection.javaClass)
                    sendPacketMethod = if (MinecraftVersion.isHigherOrEqual(MinecraftVersion.V1_18)) {
                        try {
                            reflexClass.getMethod("send", true, true, packet)
                        } catch (_: NoSuchMethodException) {
                            reflexClass.getMethod("sendPacket", true, true, packet)
                        }
                    } else {
                        reflexClass.getMethod("sendPacket", true, true, packet)
                    }
                    isFallbackMethod = false
                } catch (e: Exception) {
                    // 如果 ReflexClass 获取方法失败，使用回退方案
                    sendPacketMethod = getSendPacketMethodHybrid(connection, packet)
                    isFallbackMethod = true
                }
            }
        }

        // 根据方法类型调用
        if (isFallbackMethod) {
            (sendPacketMethod as java.lang.reflect.Method).invoke(connection, packet)
        } else {
            (sendPacketMethod as ClassMethod).invoke(connection, packet)
        }
    }

    /**
     * 获取发送数据包方法的回退方案
     * 用于处理混合服务端（CatServer等）的兼容性问题
     *
     * 注意：在混合服务端下必须使用原生 Java 反射
     * 因为 TabooLib 的 Reflex API 会尝试获取父类和返回类型
     * 这会触发 Class.forName() 加载混淆的类名，导致 CatServer 的 remapper 报错
     */
    private fun getSendPacketMethodHybrid(connection: Any, packet: Any): java.lang.reflect.Method {
        val connectionClass = connection.javaClass

        val methodNames = if (MinecraftVersion.isHigherOrEqual(MinecraftVersion.V1_18)) {
            listOf("send", "a", "sendPacket")
        } else {
            listOf("sendPacket", "a", "send")
        }

        // 首先尝试查找接受 Packet 基类或接口的方法（更通用）
        for (methodName in methodNames) {
            for (method in connectionClass.declaredMethods) {
                if (method.name == methodName && method.parameterCount == 1) {
                    val paramType = method.parameterTypes[0]
                    // 检查参数类型是否是 Packet 基类或接口
                    if (paramType.isAssignableFrom(packet.javaClass)) {
                        method.isAccessible = true
                        return method
                    }
                }
            }
        }

        // 如果找不到通用方法，尝试精确匹配具体的数据包类型
        for (methodName in methodNames) {
            try {
                val method = connectionClass.getDeclaredMethod(methodName, packet.javaClass)
                method.isAccessible = true
                // 缓存找到的方法
                return method
            } catch (_: NoSuchMethodException) {
                // 尝试下一个方法名
            }
        }

        throw NoSuchMethodException(
            "Failed to find sendPacket method. " +
                    "Connection type: ${connectionClass.name}, " +
                    "Packet type: ${packet.javaClass.name}, " +
                    "Server: ${if (MinecraftVersion.isCatServer) "Hybrid Server" else "Standard Server"}"
        )
    }

    /**
     * 获取玩家的连接实例，如果不存在则会抛出 [NullPointerException]
     */
    fun getConnection(player: Player): Any {
        return if (playerConnectionMap.containsKey(player.name)) {
            playerConnectionMap[player.name]!!
        } else {
            val connection = if (!MinecraftVersion.isCatServer) {
                // 标准方式获取连接
                if (MinecraftVersion.isUniversal) {
                    player.getProperty<Any>("entity/connection")!!
                } else {
                    player.getProperty<Any>("entity/playerConnection")!!
                }
            } else {
                // 因为 getProperty 会尝试获取字段类型，触发 Class.forName() 导致 ClassNotFoundException
                getConnectionHybrid(player)
            }
            playerConnectionMap[player.name] = connection
            connection
        }
    }

    /**
     * 获取玩家连接的回退方案
     * 用于处理混合服务端（CatServer等）的兼容性问题
     *
     * 注意：在混合服务端下必须使用原生 Java 反射
     * 因为 TabooLib 的 Reflex API 会尝试获取返回类型和字段类型
     * 这会触发 Class.forName() 加载混淆的类名，导致 CatServer 的 remapper 报错
     */
    private fun getConnectionHybrid(player: Player): Any {
        try {
            val playerClass = player.javaClass

            val getHandleMethod = playerClass.getDeclaredMethod("getHandle")
            getHandleMethod.isAccessible = true

            val entityPlayer = getHandleMethod.invoke(player)
            val entityPlayerClass = entityPlayer.javaClass

            val fieldNames = if (MinecraftVersion.isUniversal) {
                listOf("connection", "b", "c", "playerConnection")
            } else {
                listOf("playerConnection", "b", "c", "connection")
            }

            for (fieldName in fieldNames) {
                try {
                    val field = entityPlayerClass.getDeclaredField(fieldName)
                    field.isAccessible = true
                    val connection = field.get(entityPlayer)
                    if (connection != null) {
                        // 缓存找到的字段
                        return connection
                    }
                } catch (_: NoSuchFieldException) {
                    continue
                } catch (_: IllegalAccessException) {
                    continue
                }
            }

            for (field in entityPlayerClass.declaredFields) {
                val fieldType = field.type.simpleName
                if (fieldType.contains("Connection") || fieldType.contains("PlayerConnection")) {
                    try {
                        field.isAccessible = true
                        val connection = field.get(entityPlayer)
                        if (connection != null) {
                            return connection
                        }
                    } catch (_: Exception) {
                        continue
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 还找不到就真没招了
        throw RuntimeException(
            "Failed to get player connection. " +
                    "Server: ${if (MinecraftVersion.isCatServer) "Hybrid Server" else "Standard Server"}, " +
                    "Version: ${MinecraftVersion.runningVersion}"
        )
    }

    @SubscribeEvent
    private fun onJoin(e: PlayerJoinEvent) {
        playerConnectionMap.remove(e.player.name)
    }

    @SubscribeEvent
    private fun onQuit(e: PlayerQuitEvent) {
        submitAsync(delay = 20) { playerConnectionMap.remove(e.player.name) }
    }
}