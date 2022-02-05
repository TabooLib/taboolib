package taboolib.platform

import de.dytanic.cloudnet.CloudNet
import de.dytanic.cloudnet.command.Command
import de.dytanic.cloudnet.command.ICommandSender
import de.dytanic.cloudnet.command.ITabCompleter
import de.dytanic.cloudnet.common.Properties
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.*
import taboolib.common.platform.function.adaptCommandSender
import taboolib.common.platform.function.console
import taboolib.common.platform.service.PlatformCommand

/**
 * TabooLib
 * taboolib.platform.BungeeCommand
 *
 * @author sky
 * @since 2021/7/3 1:03 上午
 */
@Awake
@PlatformSide([Platform.CLOUDNET_V3])
class CloudNetV3Command : PlatformCommand {

    val plugin: CloudNetV3Plugin
        get() = CloudNetV3Plugin.getInstance()

    private val commands = mutableListOf<String>()

    override fun registerCommand(
        command: CommandInfo,
        executor: CommandExecutor,
        completer: CommandCompleter,
        component: taboolib.common.platform.command.Component.() -> Unit
    ) {
        commands.add(command.name)
        val permission = command.permission.ifEmpty { "${plugin.name}.command.use" }
        CloudNet.getInstance().commandMap.registerCommand(object : Command(arrayOf(command.name), permission), ITabCompleter {
            override fun execute(sender: ICommandSender, label: String, args: Array<String>, commandLine: String, properties: Properties) {
                executor.execute(adaptCommandSender(sender), command, command.name, args)
            }

            override fun complete(commandLine: String, args: Array<String>, properties: Properties): MutableCollection<String> {
                return completer.execute(console(), command, command.name, args)?.toMutableList() ?: ArrayList()
            }
        })
    }

    override fun unregisterCommand(command: String) {
        CloudNet.getInstance().commandMap.unregisterCommand(command)
    }

    override fun unregisterCommands() {
        commands.forEach { unregisterCommand(it) }
    }

    override fun unknownCommand(sender: ProxyCommandSender, command: String, state: Int) {

        when (state) {
            1 -> sender.cast<ICommandSender>().sendMessage(Component.translatable("command.unknown.command", TextColor.color(0xFF5555)).legacy)
            2 -> sender.cast<ICommandSender>().sendMessage(Component.translatable("command.unknown.command", TextColor.color(0xFF5555)).legacy)
            else -> return
        }
        val components = ArrayList<Component>()
        components += Component.text(command)
        components += Component.translatable("command.context.here", TextColor.color(0xFF5555), TextDecoration.ITALIC)
        sender.cast<ICommandSender>().sendMessage(Component.join(Component.empty(), components).legacy)
    }

    private val Component.legacy: String
        get() = LegacyComponentSerializer.legacy(LegacyComponentSerializer.SECTION_CHAR).serialize(this)
}