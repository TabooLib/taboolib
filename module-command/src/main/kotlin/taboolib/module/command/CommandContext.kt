package taboolib.module.command

/**
 * TabooLib
 * taboolib.module.command.CommandContext
 *
 * @author sky
 * @since 2021/6/25 10:02 上午
 */
class CommandContext(val command: taboolib.common.platform.Command, val name: String, val args: Array<String>)