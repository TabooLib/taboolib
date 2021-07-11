package taboolib.platform.type


import net.kyori.adventure.text.Component
import org.spongepowered.api.Sponge
import org.spongepowered.api.SystemSubject
import taboolib.common.platform.ProxyConsole

/**
 * TabooLib
 * taboolib.platform.type.SpongeConsole
 *
 * @author tr
 * @since 2021/6/21 15:29
 */
class Sponge8Console(val sender: SystemSubject) : ProxyConsole {

    override val origin: Any
        get() = sender

    override val name: String
        get() = "console"

    override var isOp: Boolean
        get() = true
        set(_) {
        }

    override fun sendMessage(message: String) {
        sender.sendMessage(Component.text(message))
    }

    override fun performCommand(command: String): Boolean {
        return Sponge.server().commandManager().process(Sponge.server().game().systemSubject(), command).isSuccess
    }

    override fun hasPermission(permission: String): Boolean {
        return true
    }
}