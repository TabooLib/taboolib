package taboolib.common.util

/**
 * TabooLib
 * taboolib.module.util.VariableReader
 *
 * @author sky
 * @since 2021/6/21 3:45 下午
 */
class VariableReader(val start: String = "{{", val end: String = "}}") {

    data class Part(val text: String, val isVariable: Boolean)

    /**
     * 替换嵌套变量
     */
    fun replaceNested(source: String, transfer: String.() -> String): String {
        var str = source
        while (true) {
            val endPos = indexOf(str, end)
            if (endPos == -1) {
                break
            }
            val startPos = lastIndexOf(str.substring(0, endPos), start)
            if (startPos == -1) {
                break
            }
            val before = str.substring(0, startPos)
            val after = str.substring(endPos + end.length)
            val body = transfer(format(str.substring(startPos + start.length, endPos)))
            str = before + body + after
        }
        return format(str)
    }

    fun readToFlatten(source: String): List<Part> {
        var str = source
        val parts = ArrayList<Part>()
        while (true) {
            val startPos = indexOf(str, start)
            val endPos = indexOf(str, end, startPos)
            if (startPos == -1 || endPos == -1) {
                break
            }
            if (startPos > 0) {
                parts += Part(format(str.substring(0, startPos)), false)
            }
            parts += Part(format(str.substring(startPos + start.length, endPos)), true)
            str = str.substring(endPos + end.length)
        }
        if (str.isNotEmpty()) {
            parts += Part(format(str), false)
        }
        return parts
    }

    private fun format(str: String): String {
        // 不使用 replace 将 "\" + start 和 "\" + end 替换为 start" 和 end
        return buildString {
            var i = 0
            while (i < str.length) {
                if (str[i] == '\\') {
                    // 完整匹配 start 和 end
                    if (i + start.length < str.length && str.substring(i + 1, i + start.length + 1) == start) {
                        append(start)
                        i += start.length + 1
                    } else if (i + end.length < str.length && str.substring(i + 1, i + end.length + 1) == end) {
                        append(end)
                        i += end.length + 1
                    } else {
                        append(str[i])
                        i++
                    }
                } else {
                    append(str[i])
                    i++
                }
            }
        }
    }

    private fun indexOf(source: String, str: String, start: Int = 0): Int {
        var s = start
        while (true) {
            val find = source.indexOf(str, s)
            if (find == 0 || (find > 0 && source[find - 1] != '\\')) {
                return find
            } else if (find == -1) {
                return -1
            } else {
                s = find + str.length
            }
        }
    }

    private fun lastIndexOf(source: String, str: String, start: Int = source.length): Int {
        var s = start
        while (true) {
            val find = source.lastIndexOf(str, s)
            if (find == 0 || (find > 0 && source[find - 1] != '\\')) {
                return find
            } else if (find == -1) {
                return -1
            } else {
                s = find - str.length
            }
        }
    }
}