package taboolib.platform.type

import net.afyer.afybroker.server.Broker
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.function.info

/**
 * TabooLib
 * taboolib.platform.type.BungeeConsole
 *
 * @author CziSKY
 * @since 2021/6/21 13:35
 */
object AfyBrokerCommandSender : ProxyCommandSender {

    override val origin: Any
        get() = this

    override val name: String
        get() = "Console"

    override var isOp: Boolean
        get() = error("Unsupported")
        set(_) {
            error("Unsupported")
        }

    override fun isOnline(): Boolean {
        return true
    }

    override fun sendMessage(message: String) {
        info(message)
    }

    override fun performCommand(command: String): Boolean {
        return Broker.getPluginManager().dispatchCommand(command)
    }

    override fun hasPermission(permission: String): Boolean {
        return true
    }
}