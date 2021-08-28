@file:Isolated
package taboolib.module.chat

import com.google.common.base.Strings
import net.md_5.bungee.api.ChatColor
import taboolib.common.Isolated

fun String.colored() = HexColor.translate(this)

fun String.uncolored() = ChatColor.stripColor(this.colored())!!

fun List<String>.colored() = map { it.colored() }

fun List<String>.uncolored() = map { it.uncolored() }

/**
 * 百分比状态
 * @param current 当前值
 * @param max 最大值
 * @param totalBars 总值
 * @param symbol 标识符
 * @param completedColor 完成的颜色
 * @param notCompletedColor 未完成的颜色
 * @return
 */
fun progressBar(
    current: Int, max: Int, totalBars: Int, symbol:
    String, completedColor: String,
    notCompletedColor: String): String {
    val now = if (current <= max) current else max
    val percent = now.toFloat() / max
    val progressBars = (totalBars * percent).toInt()
    return (Strings.repeat("" + completedColor + symbol, progressBars)
            + Strings.repeat("" + notCompletedColor + symbol, totalBars - progressBars))
}