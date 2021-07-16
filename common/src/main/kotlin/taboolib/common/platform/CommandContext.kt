package taboolib.common.platform

import java.util.*

/**
 * TabooLib
 * taboolib.module.command.CommandContext
 *
 * @author sky
 * @since 2021/6/25 10:02 上午
 */
class CommandContext(val sender: ProxyCommandSender, val command: CommandStructure, val name: String, val args: Array<String>)