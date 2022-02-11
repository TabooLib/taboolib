@file:Isolated

package taboolib.common5.util

import taboolib.common.Isolated
import java.util.*
import java.util.concurrent.TimeUnit

fun String.parseMillis(unit: TimeUnit = TimeUnit.MILLISECONDS): Long {
    var time = 0L
    val matches = DURATION_REGEX.findAll(this)
    // 传递过来的字符串不够纯, 例如 19dms19h810s
    if (matches.sumOf { it.value.length } != length) {
        return 0
    }
    matches.forEach { result ->
        val duration = result.groupValues[1].toLong()
        time += when (result.groupValues[2]) {
            "d" -> TimeUnit.DAYS
            "h" -> TimeUnit.HOURS
            "m" -> TimeUnit.MINUTES
            "s" -> TimeUnit.SECONDS
            else -> return@forEach // 未知的单位诶
        }.toSeconds(duration)
    }
    return unit.convert(time, TimeUnit.SECONDS)
}

@Suppress("SpellCheckingInspection")
private val DURATION_REGEX = "(\\d+)([dhms])".toRegex(RegexOption.IGNORE_CASE)