package taboolib.platform.type

import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.chat.TextComponent
import taboolib.common.platform.ProxyCommandSender
import taboolib.platform.BungeePlugin

/**
 * TabooLib
 * taboolib.platform.type.BungeeConsole
 *
 * @author CziSKY
 * @since 2021/6/21 13:35
 */
class BungeeCommandSender(val sender: CommandSender) : ProxyCommandSender {

    override val origin: Any
        get() = sender

    override val name: String
        get() = sender.name

    override var isOp: Boolean
        get() = error("unsupported")
        set(_) {
            error("unsupported")
        }

    override fun isOnline(): Boolean {
        return true
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