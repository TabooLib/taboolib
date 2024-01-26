package taboolib.common5.util

import java.util.*

/**
 * 从字符串中获取时间跨度（1d1h、1h30m）
 *
 * @return 时间跨度（单位：毫秒）
 */
fun String.parseMillis(): Long {
    var time = 0L
    var num = ""
    lowercase(Locale.getDefault()).forEach {
        if (it == '.' || it.toString().toIntOrNull() != null) {
            num += it
        } else {
            when (it) {
                'd' -> time += (num.toDouble() * 86400000).toLong()
                'h' -> time += (num.toDouble() * 3600000).toLong()
                'm' -> time += (num.toDouble() * 60000).toLong()
                's' -> time += (num.toDouble() * 1000).toLong()
            }
            num = ""
        }
    }
    return time
}