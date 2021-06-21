package taboolib.platform.type

import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.chat.ComponentSerializer
import taboolib.common.platform.ProxyPlayer
import taboolib.common.util.Location
import taboolib.platform.BungeePlugin
import java.net.InetSocketAddress
import java.util.*

/**
 * TabooLib
 * taboolib.platform.type.BungeePlayer
 *
 * @author CziSKY
 * @since 2021/6/21 13:41
 */
class BungeePlayer(val player: ProxiedPlayer) : ProxyPlayer {

    override val origin: Any
        get() = player

    override val name: String
        get() = player.name

    override val address: InetSocketAddress?
        get() = player.address

    override val uniqueId: UUID
        get() = player.uniqueId

    override val ping: Int
        get() = player.ping

    override val locale: String
        get() = player.locale.displayName

    override val world: String
        get() = error("unsupported")

    override val location: Location
        get() = error("unsupported")

    override fun kick(message: String?) {
        player.disconnect(TextComponent(message))
    }

    override fun chat(message: String) {
        player.chat(message)
    }

    override fun playSound(location: Location, sound: String, volume: Float, pitch: Float) {
        error("unsupported")
    }

    override fun playSoundResource(location: Location, sound: String, volume: Float, pitch: Float) {
        error("unsupported")
    }

    override fun sendTitle(title: String?, subtitle: String?, fadein: Int, stay: Int, fadeout: Int) {
        val titleMessage = BungeePlugin.instance.proxy.createTitle().also {
            it.title(TextComponent(title ?: ""))
            it.subTitle(TextComponent(title ?: ""))
            it.fadeIn(fadein)
            it.stay(stay)
            it.fadeOut(fadeout)
        }
        titleMessage.send(player)
    }

    override fun sendActionBar(message: String) {
        player.sendMessage(ChatMessageType.ACTION_BAR, TextComponent(message))
    }

    override fun sendRawMessage(message: String) {
        sendMessage(message)
    }

    override fun sendMessage(message: String) {
        player.sendMessage(TextComponent(*ComponentSerializer.parse(message)))
    }

    override fun performCommand(command: String): Boolean {
        return BungeePlugin.instance.proxy.pluginManager.dispatchCommand(player, command)
    }

    override fun hasPermission(permission: String): Boolean {
        return player.hasPermission(permission)
    }

    override fun teleport(loc: Location) {
        error("unsupported")
    }
}