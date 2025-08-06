package taboolib.platform.util

import taboolib.common.platform.function.info
import taboolib.common.platform.function.severe
import taboolib.common.platform.function.warning

/**
 * Paper Component Logger 工具类
 * 为 Paper 1.20.6+ 环境提供支持颜色代码的日志功能
 * 
 * 示例用法:
 * ```kotlin
 * info("&a这是绿色文本")
 * warning("&e这是黄色警告")
 * severe("&c这是红色错误")
 * ```
 * 
 * 在 Paper 环境下会自动使用 Component 系统，在其他环境下使用传统日志
 * 
 * @author TabooLib
 * @since 2025/08/06
 */
object PaperLogger {
    
    /**
     * 输出信息日志，支持颜色代码
     */
    fun info(message: String) {
        info(message)
    }
    
    /**
     * 输出警告日志，支持颜色代码
     */
    fun warning(message: String) {
        warning(message)
    }
    
    /**
     * 输出错误日志，支持颜色代码
     */
    fun severe(message: String) {
        severe(message)
    }
}

/**
 * String 扩展函数，提供更便捷的日志记录方式
 */
fun String.logInfo() = info(this)
fun String.logWarning() = warning(this)
fun String.logSevere() = severe(this)