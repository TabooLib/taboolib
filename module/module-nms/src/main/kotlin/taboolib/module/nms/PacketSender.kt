package taboolib.module.nms

import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerQuitEvent
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.reflect.Reflex.Companion.getProperty
import taboolib.common.reflect.ReflexClass
import java.lang.reflect.Method
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
    lateinit var sendPacketMethod: Method

    fun sendPacket(player: Player, packet: Any) {
        val playerConnection = getConnection(player)
        if (!::sendPacketMethod.isInitialized) {
            val reflexClass = ReflexClass.find(playerConnection.javaClass)
            sendPacketMethod = if (MinecraftVersion.major >= 10) {
                reflexClass.findMethod("send", packet)!!
            } else {
                reflexClass.findMethod("sendPacket", packet)!!
            }
            sendPacketMethod.isAccessible = true
        }
        sendPacketMethod.invoke(playerConnection, packet)
    }

    fun getConnection(player: Player): Any {
        return if (playerConnectionMap.containsKey(player.name)) {
            playerConnectionMap[player.name]!!
        } else {
            val connection = if (MinecraftVersion.isUniversal) {
                getProperty<Any>("entity/connection")!!
            } else {
                getProperty<Any>("entity/playerConnection")!!
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