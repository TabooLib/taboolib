package taboolib.platform.type

import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.command.ConsoleCommandSender
import taboolib.common.platform.ProxyConsole
import taboolib.platform.BungeePlugin

/**
 * TabooLib
 * taboolib.platform.type.BungeeConsole
 *
 * @author CziSKY
 * @since 2021/6/21 13:35
 */
class BungeeConsole(val sender: ConsoleCommandSender) : ProxyConsole {

    override val origin: Any
        get() = sender

    override val name: String
        get() = sender.name

    override var isOp: Boolean
        get() = error("unsupported")
        set(_) {
            error("unsupported")
        }

    override fun sendMessage(message: String) {
        sender.sendMessage(TextComponent(message))
    }

    override fun performCommand(command: String): Boolean {
        return BungeePlugin.getInstance().proxy.pluginManager.dispatchCommand(sender, command)
    }

    override fun hasPermission(permission: String): Boolean {
        return sender.hasPermission(permission)
    }
}