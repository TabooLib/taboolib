package taboolib.platform.type

import de.dytanic.cloudnet.CloudNet
import de.dytanic.cloudnet.command.ICommandSender
import taboolib.common.platform.ProxyCommandSender

/**
 * TabooLib
 * taboolib.platform.type.BungeeConsole
 *
 * @author CziSKY
 * @since 2021/6/21 13:35
 */
class CloudNetV3CommandSender(val sender: ICommandSender) : ProxyCommandSender {

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