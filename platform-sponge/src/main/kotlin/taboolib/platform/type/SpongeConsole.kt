package taboolib.platform.type

import org.spongepowered.api.Sponge
import org.spongepowered.api.command.source.ConsoleSource
import org.spongepowered.api.text.Text
import taboolib.common.platform.ProxyConsole

/**
 * TabooLib
 * taboolib.platform.type.SpongeConsole
 *
 * @author tr
 * @since 2021/6/21 15:29
 */
class SpongeConsole(val sender: ConsoleSource) : ProxyConsole {

    override val origin: Any
        get() = sender

    override val name: String
        get() = sender.name

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