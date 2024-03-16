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
import taboolib.common.platform.function.submit
import taboolib.module.nms.TinyReflection.ConstructorInvoker
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
    private var sendPacketMethod: ClassMethod? = null
    private var newPacketBundlePacket: ConstructorInvoker? = null

    init {
        try {
            val bundlePacketClass = TinyReflection.getClass("net.minecraft.network.protocol.game.ClientboundBundlePacket")
            newPacketBundlePacket = TinyReflection.getConstructor(bundlePacketClass, Iterable::class.java)
        } catch (ignored: Exception) {
        }
    }

    /**
     * 创建混合包（我也不知道这东西应该翻译成什么）
     */
    fun createBundlePacket(packets: List<Any>): Any? {
        return newPacketBundlePacket?.invoke(packets)
    }

    /**
     * 发送数据包
     * @param player 玩家
     * @param packet 数据包实例
     */
    fun sendPacket(player: Player, packet: Any) {
        // 如果 TinyProtocol 已经被初始化，则使用 TinyProtocol 发送数据包
        if (ProtocolHandler.instance != null) {
            ProtocolHandler.instance!!.sendPacket(player, packet)
            return
        }
        // 否则使用反射发送数据包
        val connection = getConnection(player)
        if (sendPacketMethod == null) {
            val reflexClass = ReflexClass.of(connection.javaClass)
            // 1.18 更名为 send 方法
            sendPacketMethod = if (MinecraftVersion.isHigherOrEqual(MinecraftVersion.V1_18)) {
                try {
                    reflexClass.getMethod("send", true, true, packet)
                } catch (_: NoSuchMethodException) {
                    reflexClass.getMethod("sendPacket", true, true, packet)
                }
            } else {
                reflexClass.getMethod("sendPacket", true, true, packet)
            }
        }
        sendPacketMethod!!.invoke(connection, packet)
    }

    /**
     * 获取玩家的连接实例，如果不存在则会抛出 [NullPointerException]
     */
    fun getConnection(player: Player): Any {
        return if (playerConnectionMap.containsKey(player.name)) {
            playerConnectionMap[player.name]!!
        } else {
            val connection = if (MinecraftVersion.isUniversal) {
                player.getProperty<Any>("entity/connection")!!
            } else {
                player.getProperty<Any>("entity/playerConnection")!!
            }
            playerConnectionMap[player.name] = connection
            connection
        }
    }

    @SubscribeEvent
    private fun onJoin(e: PlayerJoinEvent) {
        playerConnectionMap.remove(e.player.name)
    }

    @SubscribeEvent
    private fun onQuit(e: PlayerQuitEvent) {
        submit(delay = 20) { playerConnectionMap.remove(e.player.name) }
    }
}