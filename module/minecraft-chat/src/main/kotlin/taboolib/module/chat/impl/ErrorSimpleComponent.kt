package taboolib.module.chat.impl

import taboolib.common.io.groupId
import taboolib.module.chat.ComponentText
import taboolib.module.chat.SimpleComponent
import taboolib.module.chat.StandardColors
import taboolib.module.chat.TextTransfer
import java.io.PrintWriter
import java.io.StringWriter

/**
 * TabooLib
 * taboolib.module.chat.impl.ErrorSimpleComponent
 *
 * @author 坏黑
 * @since 2023/2/9 21:53
 */
class ErrorSimpleComponent(val e: Throwable) : SimpleComponent {

    val error: String

    init {
        val sw = StringWriter()
        e.printStackTrace(PrintWriter(sw))
        error = sw.toString().lines().filter {
            if (it.contains("at ")) {
                it.contains(groupId)
            } else {
                true
            }
        }.joinToString("\n") {
            if (it.contains("at ")) {
                val source = it.substringAfter("at $groupId.")
                val path = source.substringBeforeLast('(')
                val classPath = path.substringBeforeLast('.').substringBeforeLast('.')
                val className = path.substringBeforeLast('.').substringAfterLast('.')
                val methodName = path.substringAfterLast('.')
                val fileLine = source.substringAfterLast('(').substringBeforeLast(')').substringAfter(':')
                if (classPath == className) {
                    "  §8... §f§n$className§8 # §f$methodName() §8··· §7$fileLine"
                } else {
                    "  §8... §7$classPath.§f§n$className§8 # §f$methodName() §8··· §7$fileLine"
                }
            } else {
                "§c$it"
            }
        }
    }

    /**
     * [Missing color for gradient.]
     */
    override fun build(transfer: TextTransfer.() -> Unit): ComponentText {
        return ComponentText.empty().append("[${if (e is NullPointerException) "Internal error" else e.message}]").color(StandardColors.RED).hoverText(error)
    }
}