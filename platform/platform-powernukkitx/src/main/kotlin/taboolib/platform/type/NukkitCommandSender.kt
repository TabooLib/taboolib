package taboolib.platform.type

import cn.nukkit.Player
import cn.nukkit.command.CommandSender
import cn.nukkit.event.player.PlayerCommandPreprocessEvent
import cn.nukkit.event.server.ServerCommandEvent
import taboolib.common.platform.ProxyCommandSender
import taboolib.platform.NukkitPlugin

/**
 * TabooLib
 * starslib.platform.type.NukkitCommandSender
 *
 * @author CziSKY
 * @since 2021/6/19 23:52
 */
class NukkitCommandSender(val sender: CommandSender) : ProxyCommandSender {

    override val origin: Any
        get() = sender

    override val name: String
        get() = sender.name

    override var isOp: Boolean
        get() = sender.isOp
        set(value) {
            sender.isOp = value
        }

    override fun isOnline(): Boolean {
        return true
    }

    override fun sendMessage(message: String) {
        sender.sendMessage(message)
    }

    override fun performCommand(command: String): Boolean {
        return dispatchCommand(sender, command)
    }

    override fun hasPermission(permission: String): Boolean {
        return sender.hasPermission(permission)
    }

    fun dispatchCommand(sender: CommandSender, command: String): Boolean {
        if (sender is Player) {
            val event = PlayerCommandPreprocessEvent((sender as Player?)!!, "/$command")
            NukkitPlugin.getInstance().server.pluginManager.callEvent(event)
            if (!event.isCancelled && event.message.isNotBlank() && event.message.startsWith("/")) {
                return NukkitPlugin.getInstance().server.dispatchCommand(event.player, event.message.substring(1))
            }
        } else {
            val e = ServerCommandEvent(sender, command)
            NukkitPlugin.getInstance().server.pluginManager.callEvent(e)
            if (!e.isCancelled && e.command.isNotBlank()) {
                return NukkitPlugin.getInstance().server.dispatchCommand(e.sender, e.command)
            }
        }
        return false
    }
}