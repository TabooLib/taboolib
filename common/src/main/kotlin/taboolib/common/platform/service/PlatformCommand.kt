package taboolib.common.platform.service

import taboolib.common.platform.PlatformService
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.CommandBuilder
import taboolib.common.platform.command.CommandCompleter
import taboolib.common.platform.command.CommandExecutor
import taboolib.common.platform.command.CommandStructure

/**
 * TabooLib
 * taboolib.common.platform.service.PlatformCommand
 *
 * @author sky
 * @since 2021/6/24 11:46 下午
 */
@PlatformService
interface PlatformCommand {

    /**
     *  注册一个命令
     *
     *  @param command 命令结构
     *  @param executor 执行器
     *  @param completer 补全器
     *  @param commandBuilder 命令构建器
     */
    fun registerCommand(command: CommandStructure, executor: CommandExecutor, completer: CommandCompleter, commandBuilder: CommandBuilder.CommandBase.() -> Unit)

    /**
     * 注销一个命令
     *
     * @param command 命令名称
     */
    fun unregisterCommand(command: String)

    /**
     * 注销所有命令
     */
    fun unregisterCommands()

    /**
     * 发送未知命令消息
     */
    fun unknownCommand(sender: ProxyCommandSender, command: String, state: Int)

    /**
     * 是否支持未知命令消息
     */
    fun isSupportedUnknownCommand() = false

    companion object {

        /**
         * 默认无权限消息
         */
        const val defaultPermissionMessage = "§cI'm sorry, but you do not have permission to perform this command. Please contact the server administrators if you believe that this is in error."
    }
}