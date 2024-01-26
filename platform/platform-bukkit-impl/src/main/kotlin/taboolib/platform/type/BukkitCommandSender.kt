package taboolib.platform.type

import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.server.ServerCommandEvent
import taboolib.common.platform.ProxyCommandSender

/**
 * TabooLib
 * taboolib.platform.type.BukkitConsole
 *
 * @author sky
 * @since 2021/6/17 10:35 下午
 */
class BukkitCommandSender(val sender: CommandSender) : ProxyCommandSender {

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
            val event = PlayerCommandPreprocessEvent(sender, "/$command")
            Bukkit.getPluginManager().callEvent(event)
            if (!event.isCancelled && event.message.isNotBlank() && event.message.startsWith("/")) {
                return Bukkit.dispatchCommand(event.player, event.message.substring(1))
            }
        } else {
            val e = ServerCommandEvent(sender, command)
            Bukkit.getPluginManager().callEvent(e)
            if (!e.isCancelled && e.command.isNotBlank()) {
                return Bukkit.dispatchCommand(e.sender, e.command)
            }
        }
        return false
    }
}