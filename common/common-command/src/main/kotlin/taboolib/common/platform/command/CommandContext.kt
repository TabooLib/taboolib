package taboolib.common.platform.command

import taboolib.common.platform.ProxyCommandSender

/**
 * @author sky
 * @since 2021/6/25 10:02 上午
 */
data class CommandContext<T>(
    val sender: T,
    val command: Command,
    val name: String,
    val compound: Component,
    internal val args: Array<String>,
    internal var index: Int = 0,
) {

    fun checkPermission(permission: String): Boolean {
        return (sender as ProxyCommandSender).hasPermission(permission)
    }

    /**
     * 取全部参数
     */
    fun args(): Array<String> {
        return args.filterIndexed { i, _ -> i <= index }.toTypedArray().also {
            it[index] = "${it[index]} ${args.filterIndexed { i, _ -> i > index }.joinToString(" ")}".trim()
        }
    }

    /**
     * 取绝对位置参数
     */
    fun get(index: Int): String {
        return args()[index]
    }

    fun getOrNull(index: Int): String? {
        return kotlin.runCatching { get(index) }.getOrNull()
    }

    /**
     * 取相对位置参数
     */
    fun argument(offset: Int): String {
        return args()[index + offset]
    }

    fun argumentOrNull(offset: Int): String? {
        return kotlin.runCatching { argument(offset) }.getOrNull()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CommandContext<*>) return false
        if (sender != other.sender) return false
        if (command != other.command) return false
        if (name != other.name) return false
        if (compound != other.compound) return false
        if (!args.contentEquals(other.args)) return false
        if (index != other.index) return false
        return true
    }

    override fun hashCode(): Int {
        var result = sender?.hashCode() ?: 0
        result = 31 * result + command.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + compound.hashCode()
        result = 31 * result + args.contentHashCode()
        result = 31 * result + index
        return result
    }
}