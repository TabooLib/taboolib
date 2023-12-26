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
import taboolib.common.platform.event.EventPriority
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
    private var isDisabled = false

    /**
     * 禁用 Channel 注入
     */
    fun disable() {
        isDisabled = true
    }

    /**
     * 数据包事件是否被当前插件监听
     */
    fun isPacketEventListened(): Boolean {
        return PacketSendEvent::class.java.isListened() || PacketReceiveEvent::class.java.isListened()
    }

    /**
     * 获取玩家的 [Channel]
     */
    fun getPlayerChannel(address: InetAddress, isFirst: Boolean): Channel {
        return nmsProxy<ConnectionGetter>().getChannel(address, isFirst)
    }

    /**
     * 将 TabooLib 的 [ChannelHandler] 注入到玩家的 [Channel] 中，只有插件监听了 [PacketSendEvent] 或 [PacketReceiveEvent] 时才会执行。
     */
    fun addPlayerChannel(player: Player, address: InetAddress) {
        if (isDisabled || !isPacketEventListened()) {
            return
        }
        if (!MinecraftVersion.isSupported) {
            warning("Unsupported Minecraft version, packet handler will not be added.")
            return
        }
        try {
            val pipeline = getPlayerChannel(address, true).pipeline()
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

    /**
     * 取消玩家的 [Channel] 注入
     * @param async 是否异步执行
     */
    fun removePlayerChannel(player: Player, async: Boolean = true) {
        if (isDisabled || !isPacketEventListened()) {
            return
        }
        if (!MinecraftVersion.isSupported) {
            warning("Unsupported Minecraft version, packet handler will not be added.")
            return
        }
        val address = player.address?.address ?: return
        fun process() {
            try {
                val pipeline = getPlayerChannel(address, false).pipeline()
                if (pipeline[id] != null) {
                    pipeline.remove(id)
                }
            } catch (ex: Throwable) {
                ex.printStackTrace()
            }
        }
        if (async) {
            pool.submit(::process)
        } else {
            process()
        }
    }

    @SubscribeEvent(EventPriority.MONITOR)
    private fun onJoin(e: PlayerLoginEvent) {
        if (e.result == PlayerLoginEvent.Result.ALLOWED) {
            addPlayerChannel(e.player, e.address)
        }
    }

    @SubscribeEvent
    private fun onQuit(e: PlayerQuitEvent) {
        if (TabooLibCommon.isStopped()) {
            return
        }
        nmsProxy<ConnectionGetter>().release(e.player.address ?: return)
    }

    @Awake(LifeCycle.ACTIVE)
    private fun onEnable() {
        if (TabooLibCommon.isStopped()) {
            return
        }
        onlinePlayers.forEach {
            val address = it.address?.address
            if (address == null) {
                warning("Cannot get player address: ${it.name} (${it.address})")
                return@forEach
            }
            addPlayerChannel(it, address)
        }
    }

    @Awake(LifeCycle.DISABLE)
    private fun onDisable() {
        if (TabooLibCommon.isStopped()) {
            return
        }
        onlinePlayers.forEach { removePlayerChannel(it, async = false) }
    }
}