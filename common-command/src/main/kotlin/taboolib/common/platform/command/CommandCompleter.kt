package taboolib.common.platform.command

import taboolib.common.platform.ProxyCommandSender

/**
 * @author sky
 * @since 2021/6/24 11:49 下午
 */
interface CommandCompleter {

    fun execute(sender: ProxyCommandSender, command: CommandInfo, name: String, args: Array<String>): List<String>?
}