@file:Isolated
package taboolib.common.util

import taboolib.common.Isolated

fun String.replaceWithOrder(vararg args: Any): String {
    if (args.isEmpty() || isEmpty()) {
        return this
    }
    val chars = toCharArray()
    val builder = StringBuilder(length)
    var i = 0
    while (i < chars.size) {
        val mark = i
        if (chars[i] == '{') {
            var num = 0
            while (i + 1 < chars.size && Character.isDigit(chars[i + 1])) {
                i++
                num *= 10
                num += chars[i] - '0'
            }
            if (i != mark && i + 1 < chars.size && chars[i + 1] == '}') {
                i++
                builder.append(args.getOrNull(num) ?: "{$num}")
            } else {
                i = mark
            }
        }
        if (mark == i) {
            builder.append(chars[i])
        }
        i++
    }
    return builder.toString()
}

fun String.decodeUnicode(): String {
    var r = this
    fun process() {
        val i = r.indexOf("\\u")
        if (i != -1) {
            r = r.substring(0, i) + Integer.parseInt(r.substring(i + 2, i + 6), 16).toChar() + r.substring(i + 6)
            process()
        }
    }
    process()
    return r
}