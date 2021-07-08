package taboolib.platform.type

import org.spongepowered.api.Server
import taboolib.common.platform.ProxyConsole

/**
 * TabooLib
 * taboolib.platform.type.SpongeConsole
 *
 * @author tr
 * @since 2021/6/21 15:29
 */
class SpongeConsole(val sender: Server) : ProxyConsole {

    override val origin: Any
        get() = sender

    override val name: String
        get() = error("unsupported ap7->api8")

    override var isOp: Boolean
        get() = error("unsupported ap7->api8")
        set(value) {
            error("unsupported ap7->api8")
        }

    override fun sendMessage(message: String) {
        error("unsupported ap7->api8")
    }

    override fun performCommand(command: String): Boolean {
        error("unsupported ap7->api8")
    }

    override fun hasPermission(permission: String): Boolean {
        error("unsupported ap7->api8")
    }
}