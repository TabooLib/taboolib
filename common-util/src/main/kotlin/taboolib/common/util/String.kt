@file:Isolated
package taboolib.common.util

import taboolib.common.Isolated
import java.util.regex.Matcher
import java.util.regex.Pattern

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
    val pattern by lazy { Pattern.compile("\\\\u[0-9a-fA-F]{4}") }

    val matcher = pattern.matcher(this)
    val sb = StringBuffer()

    while (matcher.find()) {
        val str = matcher.group()
        val hex = str.substring(2, str.length - 1)
        val unicode = Integer.parseInt(hex, 16)
        matcher.appendReplacement(sb, Matcher.quoteReplacement(String(Character.toChars(unicode))))
    }

    matcher.appendTail(sb)
    return sb.toString()
}
