package taboolib.platform.type

import com.velocitypowered.api.command.CommandSource
import net.kyori.adventure.text.Component
import taboolib.common.platform.ProxyCommandSender
import taboolib.platform.VelocityPlugin

/**
 * TabooLib
 * taboolib.platform.type.VelocityConsole
 *
 * @author CziSKY
 * @since 2021/6/21 13:35
 */
class VelocityCommandSender(val sender: CommandSource) : ProxyCommandSender {

    override val origin: Any
        get() = sender

    override val name: String
        get() = "console"

    override var isOp: Boolean
        get() = error("Unsupported")
        set(_) {
            error("Unsupported")
        }

    override fun isOnline(): Boolean {
        return true
    }

    override fun sendMessage(message: String) {
        sender.sendMessage(Component.text(message))
    }

    override fun performCommand(command: String): Boolean {
        VelocityPlugin.getInstance().server.commandManager.executeAsync(sender, command)
        return true
    }

    override fun hasPermission(permission: String): Boolean {
        return sender.hasPermission(permission)
    }
}