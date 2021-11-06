package taboolib.platform.type

import org.spongepowered.api.Sponge
import org.spongepowered.api.command.CommandSource
import org.spongepowered.api.service.permission.SubjectData
import org.spongepowered.api.text.Text
import org.spongepowered.api.util.Tristate
import taboolib.common.platform.ProxyCommandSender

/**
 * TabooLib
 * taboolib.platform.type.SpongeConsole
 *
 * @author tr
 * @since 2021/6/21 15:29
 */
class Sponge7CommandSender(val sender: CommandSource) : ProxyCommandSender {

    override val origin: Any
        get() = sender

    override val name: String
        get() = sender.name

    override var isOp: Boolean
        get() = sender.hasPermission("*")
        set(value) {
            sender.subjectData.setPermission(SubjectData.GLOBAL_CONTEXT, "*", if (value) Tristate.TRUE else Tristate.UNDEFINED)
        }

    override fun isOnline(): Boolean {
        return true
    }

    override fun sendMessage(message: String) {
        sender.sendMessage(Text.of(message))
    }

    override fun performCommand(command: String): Boolean {
        return Sponge.getCommandManager().process(sender, command).successCount.isPresent
    }

    override fun hasPermission(permission: String): Boolean {
        return sender.hasPermission(permission)
    }
}