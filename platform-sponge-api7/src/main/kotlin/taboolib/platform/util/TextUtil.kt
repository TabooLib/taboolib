@file:Isolated

package taboolib.platform.util

import com.google.common.base.Strings
import org.spongepowered.api.text.Text
import org.spongepowered.api.text.serializer.TextSerializers
import taboolib.common.Isolated

/**
 * The string with formatting code, for example: "&4Warning!"
 * @return Text object
 */
fun String.toTextWithFormattingCode(): Text {
    return TextSerializers.FORMATTING_CODE.deserialize(this)
}

/**
 * Text object
 * @return The string with formatting code, for example: "&4Warning!"
 */
fun Text.toPlainWithFormattingCode(): String {
    return TextSerializers.FORMATTING_CODE.serialize(this)
}

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