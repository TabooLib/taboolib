package taboolib.module.nms

import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerQuitEvent
import org.tabooproject.reflex.ClassMethod
import org.tabooproject.reflex.Reflex.Companion.getProperty
import org.tabooproject.reflex.ReflexClass
import taboolib.common.platform.event.SubscribeEvent
import java.util.concurrent.ConcurrentHashMap

/**
 * TabooLib
 * taboolib.module.nms.PacketSender
 *
 * @author 坏黑
 * @since 2022/7/19 16:02
 */
object PacketSender {

    private val playerConnectionMap = ConcurrentHashMap<String, Any>()
    private var sendPacketMethod: ClassMethod? = null

    fun sendPacket(player: Player, packet: Any) {
        val playerConnection = getConnection(player)
        if (sendPacketMethod == null) {
            val reflexClass = ReflexClass.of(playerConnection.javaClass)
            // 1.19 更名为 send 方法
            sendPacketMethod = if (MinecraftVersion.major >= 10) {
                reflexClass.getMethod("send", true, true, packet)
            } else {
                reflexClass.getMethod("sendPacket", true, true, packet)
            }
        }
        sendPacketMethod!!.invoke(playerConnection, packet)
    }

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
    fun e(e: PlayerQuitEvent) {
        playerConnectionMap.remove(e.player.name)
    }
}