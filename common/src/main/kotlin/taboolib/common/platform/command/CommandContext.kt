package taboolib.common.platform.command

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.ProxyPlayer
import taboolib.common.platform.command.component.CommandBase
import taboolib.common.platform.command.component.CommandComponent
import taboolib.common.platform.command.component.CommandComponentDynamic
import taboolib.common.platform.command.component.CommandComponentLiteral
import taboolib.common.platform.function.adaptCommandSender

/**
 * TabooLib
 * taboolib.module.command.CommandContext
 *
 * @author sky
 * @since 2021/6/25 10:02 上午
 */
data class CommandContext<T>(
    private val sender: T,
    val command: CommandStructure,
    val name: String,
    val commandCompound: CommandBase,
    internal val rawArgs: Array<String>,
    internal var index: Int = 0,
    internal var currentComponent: CommandComponent? = null
) {

    /**
     * 获取命令发送者
     */
    fun sender(): ProxyCommandSender {
        return if (sender is ProxyCommandSender) sender else adaptCommandSender(sender as Any)
    }

    /**
     * 获取命令发送者（强制转换为玩家类型）
     * @throws ClassCastException 如果发送者不是玩家
     */
    fun player(): ProxyPlayer {
        return sender() as ProxyPlayer
    }

    /**
     * 检查命令发送者是否持有权限
     */
    fun checkPermission(permission: String): Boolean {
        return sender().hasPermission(permission)
    }

    /**
     * 取全部参数，对当前位置之后的参数进行拼接
     */
    fun args(): Array<String> {
        return rawArgs.filterIndexed { i, _ -> i <= index }.toTypedArray().also { it[index] = "${it[index]} ${rawArgs.filterIndexed { i, _ -> i > index }.joinToString(" ")}".trim() }
    }

    /**
     * 取当前位置参数，对当前位置之后的参数进行拼接
     */
    fun self(): String {
        return args()[index]
    }

    /**
     * 根据节点名称获取输入参数（若不存在则抛出异常）
     *
     * @param id 节点名称
     * @return 指定位置的输入参数
     * @throws IllegalStateException 参数不存在
     */
    operator fun get(id: String): String {
        return getOrNull(id) ?: error("Parameter $id not found.")
    }

    /**
     * 根据节点名称获取输入参数
     *
     * @param id 节点名称
     * @return 指定位置的输入参数
     */
    fun getOrNull(id: String): String? {
        fun process(compound: CommandComponent): Int {
            val find = when (compound) {
                is CommandComponentLiteral -> compound.aliases.contains(id)
                is CommandComponentDynamic -> compound.comment == id
                else -> false
            }
            return if (find) compound.index else process(compound.parent ?: return -1)
        }
        val idx = process(currentComponent ?: return null)
        if (idx == -1) {
            return null
        }
        return args()[idx]
    }

    @Deprecated("设计过于傻逼,令人智熄", ReplaceWith("get(id)"))
    fun get(index: Int): String {
        return args()[index]
    }

    @Deprecated("设计过于傻逼,令人智熄", ReplaceWith("get(id)"))
    fun getOrNull(index: Int): String? {
        return kotlin.runCatching { get(index) }.getOrNull()
    }

    @Deprecated("设计过于傻逼,令人智熄", ReplaceWith("get(id)"))
    fun argument(offset: Int): String {
        return args()[index + offset]
    }

    @Deprecated("设计过于傻逼,令人智熄", ReplaceWith("get(id)"))
    fun argumentOrNull(offset: Int): String? {
        return kotlin.runCatching { argument(offset) }.getOrNull()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CommandContext<*>) return false
        if (sender != other.sender) return false
        if (command != other.command) return false
        if (name != other.name) return false
        if (commandCompound != other.commandCompound) return false
        if (!rawArgs.contentEquals(other.rawArgs)) return false
        if (index != other.index) return false
        if (currentComponent != other.currentComponent) return false
        return true
    }

    override fun hashCode(): Int {
        var result = sender?.hashCode() ?: 0
        result = 31 * result + command.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + commandCompound.hashCode()
        result = 31 * result + rawArgs.contentHashCode()
        result = 31 * result + index
        result = 31 * result + (currentComponent?.hashCode() ?: 0)
        return result
    }
}