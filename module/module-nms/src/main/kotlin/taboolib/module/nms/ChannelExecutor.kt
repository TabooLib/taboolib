package taboolib.module.nms

import io.netty.channel.Channel
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerLoginEvent
import org.bukkit.event.player.PlayerQuitEvent
import taboolib.common.LifeCycle
import taboolib.common.TabooLibCommon
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.isListened
import taboolib.common.platform.function.pluginId
import taboolib.common.platform.function.warning
import taboolib.platform.util.onlinePlayers
import java.net.InetAddress
import java.util.concurrent.Executors

/**
 * @author 坏黑
 * @since 2018-10-28 14:34
 */
@PlatformSide([Platform.BUKKIT])
object ChannelExecutor {

    private val id = "taboolib_${pluginId}_packet_handler"
    private val pool = Executors.newSingleThreadExecutor()

    /** 是否强制禁用 **/
    private var isDisabled = false

    fun disable() {
        isDisabled = true
    }

    /**
     * 数据包事件是否被监听
     */
    fun isPacketEventListened(): Boolean {
        return PacketSendEvent::class.java.isListened() || PacketReceiveEvent::class.java.isListened()
    }

    fun getPlayerChannel(address: InetAddress): Channel {
        val connection = ConnectionGetter.instance.getConnection(address)
        return ConnectionGetter.instance.getChannel(connection)
    }

    fun addPlayerChannel(player: Player, address: InetAddress) {
        if (isDisabled || !isPacketEventListened()) {
            return
        }
        if (!MinecraftVersion.isSupported) {
            warning("Unsupported Minecraft version, packet handler will not be added.")
            return
        }
        try {
            val pipeline = getPlayerChannel(address).pipeline()
            try {
                pipeline.remove(id)
            } catch (_: NoSuchElementException) {
            }
            if (pipeline["packet_handler"] == null) {
                pipeline.addLast(id, ChannelHandler(player))
            } else {
                pipeline.addBefore("packet_handler", id, ChannelHandler(player))
            }
        } catch (ex: Throwable) {
            ex.printStackTrace()
        }
    }

    fun removePlayerChannel(player: Player) {
        if (isDisabled || !isPacketEventListened()) {
            return
        }
        if (!MinecraftVersion.isSupported) {
            warning("Unsupported Minecraft version, packet handler will not be added.")
            return
        }
        val address = player.address?.address ?: return
        pool.submit {
            try {
                val pipeline = getPlayerChannel(address).pipeline()
                if (pipeline[id] != null) {
                    pipeline.remove(id)
                }
            } catch (ex: Throwable) {
                ex.printStackTrace()
            }
        }
    }

    @SubscribeEvent
    private fun onJoin(e: PlayerLoginEvent) {
        addPlayerChannel(e.player, e.address)
    }

    @SubscribeEvent
    private fun onQuit(e: PlayerQuitEvent) {
        if (TabooLibCommon.isStopped()) {
            return
        }
        ConnectionGetter.instance.release(e.player.address ?: return)
    }

    @Awake(LifeCycle.ENABLE)
    private fun onEnable() {
        onlinePlayers.forEach { addPlayerChannel(it, it.address?.address ?: return@forEach) }
    }

    @Awake(LifeCycle.DISABLE)
    private fun onDisable() {
        if (TabooLibCommon.isStopped()) {
            return
        }
        onlinePlayers.forEach { removePlayerChannel(it) }
    }
}