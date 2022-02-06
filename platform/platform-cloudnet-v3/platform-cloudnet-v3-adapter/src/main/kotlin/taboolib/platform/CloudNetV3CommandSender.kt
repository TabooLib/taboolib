package taboolib.platform

import de.dytanic.cloudnet.CloudNet
import de.dytanic.cloudnet.command.ICommandSender
import taboolib.common.platform.ProxyCommandSender
import taboolib.internal.Internal

/**
 * TabooLib
 * taboolib.platform.type.BungeeConsole
 *
 * @author CziSKY
 * @since 2021/6/21 13:35
 */
@Internal
class CloudNetV3CommandSender(val sender: ICommandSender) : ProxyCommandSender {

    override val origin: Any
        get() = sender

    override val name: String
        get() = sender.name

    override var isOp: Boolean
        get() = error("Unsupported")
        set(_) {
            error("Unsupported")
        }

    override fun isOnline(): Boolean {
        return true
    }

    override fun sendMessage(message: String) {
        sender.sendMessage(message)
    }

    override fun performCommand(command: String): Boolean {
        CloudNet.getInstance().nodeInfoProvider.sendCommandLineAsync(command)
        return true
    }

    override fun hasPermission(permission: String): Boolean {
        return sender.hasPermission(permission)
    }
}