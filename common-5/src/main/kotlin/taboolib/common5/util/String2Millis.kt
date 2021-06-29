@file:Isolated

package taboolib.common5.util

import taboolib.common.Isolated

fun String.parseMillis(): Long {
    var time = 0L
    var num = ""
    toLowerCase().forEach {
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