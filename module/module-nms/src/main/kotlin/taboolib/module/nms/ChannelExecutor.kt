package taboolib.module.nms

import io.netty.channel.Channel
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.tabooproject.reflex.Reflex.Companion.getProperty
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.pluginId
import taboolib.common.platform.function.warning
import taboolib.platform.util.onlinePlayers
import java.util.concurrent.Executors

/**
 * @author 坏黑
 * @since 2018-10-28 14:34
 */
@PlatformSide([Platform.BUKKIT])
object ChannelExecutor {

    private val id = "taboolib_${pluginId}_packet_handler"
    private val addChannelService = Executors.newSingleThreadExecutor()
    private val removeChannelService = Executors.newSingleThreadExecutor()

    private var isDisabled = false

    fun disable() {
        isDisabled = true
    }

    fun getPlayerChannel(player: Player): Channel {
        val playerConnection = if (MinecraftVersion.isUniversal) {
            player.getProperty<Any>("entity/connection")!!
        } else {
            player.getProperty<Any>("entity/playerConnection")!!
        }
        // playerConnection 被异常注入
        return try {
            if (MinecraftVersion.isUniversal) {
                playerConnection.getProperty<Channel>("connection/channel")!!
            } else {
                playerConnection.getProperty<Channel>("networkManager/channel")!!
            }
        } catch (ex: Throwable) {
            throw IllegalStateException("Unable to get player Channel from ${playerConnection.javaClass}", ex)
        }
    }

    fun addPlayerChannel(player: Player) {
        if (isDisabled) {
            return
        }
        if (!MinecraftVersion.isSupported) {
            warning("Unsupported Minecraft version, packet handler will not be added.")
            return
        }
        addChannelService.submit {
            try {
                getPlayerChannel(player).pipeline().addBefore("packet_handler", id, ChannelHandler(player))
            } catch (ex: Throwable) {
                ex.printStackTrace()
            }
        }
    }

    fun removePlayerChannel(player: Player) {
        if (isDisabled) {
            return
        }
        if (!MinecraftVersion.isSupported) {
            warning("Unsupported Minecraft version, packet handler will not be added.")
            return
        }
        removeChannelService.submit {
            try {
                val playerChannel = getPlayerChannel(player)
                if (playerChannel.pipeline()[id] != null) {
                    playerChannel.pipeline().remove(id)
                }
            } catch (ex: Throwable) {
                ex.printStackTrace()
            }
        }
    }

    @SubscribeEvent
    internal fun onJoin(e: PlayerJoinEvent) {
        addPlayerChannel(e.player)
    }

    @SubscribeEvent
    internal fun onQuit(e: PlayerQuitEvent) {
        removePlayerChannel(e.player)
    }

    @Awake(LifeCycle.ENABLE)
    internal fun onEnable() {
        onlinePlayers.forEach { addPlayerChannel(it) }
    }

    @Awake(LifeCycle.DISABLE)
    internal fun onDisable() {
        onlinePlayers.forEach { removePlayerChannel(it) }
    }
}