package taboolib.module.nms

import io.netty.channel.Channel
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.pluginId
import taboolib.common.reflect.Reflex.Companion.getProperty
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

    fun getPlayerChannel(player: Player): Channel {
        return if (MinecraftVersion.isUniversal) {
            player.getProperty<Channel>("entity/connection/connection/channel")!!
        } else {
            player.getProperty<Channel>("entity/playerConnection/networkManager/channel")!!
        }
    }

    fun addPlayerChannel(player: Player) {
        addChannelService.submit {
            try {
                getPlayerChannel(player).pipeline().addBefore("packet_handler", id, ChannelHandler(player))
            } catch (ex: Throwable) {
                ex.printStackTrace()
            }
        }
    }

    fun removePlayerChannel(player: Player) {
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
    internal fun e(e: PlayerJoinEvent) {
        addPlayerChannel(e.player)
    }

    @SubscribeEvent
    internal fun e(e: PlayerQuitEvent) {
        removePlayerChannel(e.player)
    }
}