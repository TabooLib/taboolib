package taboolib.platform.type

import net.kyori.adventure.text.Component
import org.spongepowered.api.Sponge
import org.spongepowered.api.SystemSubject
import org.spongepowered.api.command.source.ConsoleSource
import org.spongepowered.api.service.context.Context
import org.spongepowered.api.service.permission.SubjectData
import org.spongepowered.api.text.Text
import org.spongepowered.api.util.Tristate
import taboolib.common.platform.ProxyConsole

/**
 * TabooLib
 * taboolib.platform.type.SpongeConsole
 *
 * @author tr
 * @since 2021/6/21 15:29
 */
class SpongeConsole(val sender: SystemSubject) : ProxyConsole {
    // TODO: 2021/7/9 There is no more ConsoleSource

    override val origin: Any
        get() = sender

    // TODO: 2021/7/9 Why console have a name ? could it return "Console" directly?
    override val name: String
        get() = sender.name

    // TODO: 2021/7/9 if it is console, is there any reason that it do not have all permissions?
    override var isOp: Boolean
        get() = sender.hasPermission("*")
        set(value) {
            sender.subjectData.setPermission(SubjectData.GLOBAL_CONTEXT, "*", if (value) Tristate.TRUE else Tristate.UNDEFINED)
        }

    override fun sendMessage(message: String) {
        // try Component.text(message)
        sender.sendMessage(Component.text(message))
    }

    override fun performCommand(command: String): Boolean {
        return Sponge.server().commandManager().process(Sponge.server().game().systemSubject(), command).isSuccess
    }

    override fun hasPermission(permission: String): Boolean {
        return sender.hasPermission(permission)
    }
}