package taboolib.platform.type

import cn.nukkit.command.CommandSender
import taboolib.common.platform.ProxyCommandSender
import taboolib.platform.util.dispatchCommand

/**
 * TabooLib
 * taboolib.platform.type.NukkitCommandSender
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