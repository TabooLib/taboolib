package taboolib.platform.type

import cn.nukkit.command.ConsoleCommandSender
import taboolib.common.platform.ProxyConsole
import taboolib.platform.util.dispatchCommand

/**
 * TabooLib
 * taboolib.platform.type.NukkitConsole
 *
 * @author CziSKY
 * @since 2021/6/19 23:52
 */
class NukkitConsole(val sender: ConsoleCommandSender) : ProxyConsole {

    override val origin: Any
        get() = sender
    override val name: String
        get() = sender.name

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