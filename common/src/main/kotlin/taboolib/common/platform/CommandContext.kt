package taboolib.common.platform

import java.security.Permission

/**
 * TabooLib
 * taboolib.module.command.CommandContext
 *
 * @author sky
 * @since 2021/6/25 10:02 上午
 */
class CommandContext<T>(val sender: T, val command: CommandStructure, val name: String, val args: Array<String>, var index: Int = 0) {

    fun argument(offset: Int): String {
        return args[index + offset]
    }

    fun checkPermission(permission: String): Boolean {
        return (sender as ProxyCommandSender).hasPermission(permission)
    }
}