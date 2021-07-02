package taboolib.platform.type

import com.velocitypowered.api.proxy.ConsoleCommandSource
import net.kyori.adventure.text.Component
import taboolib.common.platform.ProxyConsole
import taboolib.platform.VelocityPlugin

/**
 * TabooLib
 * taboolib.platform.type.VelocityConsole
 *
 * @author CziSKY
 * @since 2021/6/21 13:35
 */
class VelocityConsole(val sender: ConsoleCommandSource) : ProxyConsole {

    override val origin: Any
        get() = sender

    override val name: String
        get() = "console"

    override var isOp: Boolean
        get() = error("unsupported")
        set(_) {
            error("unsupported")
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