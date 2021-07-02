package taboolib.platform.type

import com.velocitypowered.api.proxy.Player
import net.kyori.adventure.text.Component
import net.kyori.adventure.title.Title
import taboolib.common.platform.ProxyPlayer
import taboolib.common.util.Location
import taboolib.platform.VelocityPlugin
import java.net.InetSocketAddress
import java.time.Duration
import java.util.*

/**
 * TabooLib
 * taboolib.platform.type.BungeePlayer
 *
 * @author CziSKY
 * @since 2021/6/21 13:41
 */
class VelocityPlayer(val player: Player) : ProxyPlayer {

    override val origin: Any
        get() = player

    override val name: String
        get() = player.username

    override val address: InetSocketAddress?
        get() = player.remoteAddress

    override val uniqueId: UUID
        get() = player.uniqueId

    override val ping: Int
        get() = player.ping.toInt()

    override val locale: String
        get() = error("unsupported")

    override val world: String
        get() = error("unsupported")

    override val location: Location
        get() = error("unsupported")

    override var isOp: Boolean
        get() = error("unsupported")
        set(_) {
            error("unsupported")
        }

    override fun kick(message: String?) {
        player.disconnect(Component.text(message ?: ""))
    }

    override fun chat(message: String) {
        player.spoofChatInput(message)
    }

    override fun playSound(location: Location, sound: String, volume: Float, pitch: Float) {
        error("unsupported")
    }

    override fun playSoundResource(location: Location, sound: String, volume: Float, pitch: Float) {
        error("unsupported")
    }

    override fun sendTitle(title: String?, subtitle: String?, fadein: Int, stay: Int, fadeout: Int) {
        player.showTitle(Title.title(
            Component.text(title ?: ""),
            Component.text(subtitle ?: ""),
            Title.Times.of(
                Duration.ofSeconds(fadein * 50L),
                Duration.ofSeconds(stay * 50L),
                Duration.ofSeconds(fadeout * 50L)
            )
        ))
    }

    override fun sendActionBar(message: String) {
        player.sendActionBar(Component.text(message))
    }

    override fun sendRawMessage(message: String) {
        sendMessage(message)
    }

    override fun sendMessage(message: String) {
        player.sendMessage(Component.text(message))
    }

    override fun performCommand(command: String): Boolean {
        VelocityPlugin.getInstance().server.commandManager.executeAsync(player, command)
        return true
    }

    override fun hasPermission(permission: String): Boolean {
        return player.hasPermission(permission)
    }

    override fun teleport(loc: Location) {
        error("unsupported")
    }
}