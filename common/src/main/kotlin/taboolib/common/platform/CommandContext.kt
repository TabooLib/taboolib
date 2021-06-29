package taboolib.common.platform

import taboolib.common.io.Isolated

/**
 * TabooLib
 * taboolib.module.command.CommandContext
 *
 * @author sky
 * @since 2021/6/25 10:02 上午
 */
class CommandContext(val command: CommandStructure, val name: String, val args: Array<String>)