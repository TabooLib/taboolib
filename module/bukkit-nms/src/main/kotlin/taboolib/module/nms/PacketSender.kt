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
    private var sendPacketMethod: ClassMethod? = null

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