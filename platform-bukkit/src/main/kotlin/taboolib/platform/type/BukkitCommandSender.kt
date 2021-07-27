package taboolib.platform.type

import org.bukkit.command.CommandSender
import taboolib.common.platform.ProxyCommandSender
import taboolib.platform.util.dispatchCommand

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

    override fun sendMessage(message: String) {
        sender.sendMessage(message)
    }

    override fun performCommand(command: String): Boolean {
        return dispatchCommand(sender, command)
    }

    override fun hasPermission(permission: String): Boolean {
        return sender.hasPermission(permission)
    }
}