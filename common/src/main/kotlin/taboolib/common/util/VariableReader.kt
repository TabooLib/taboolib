package taboolib.common.util

import taboolib.common.Isolated

/**
 * TabooLib
 * taboolib.module.util.VariableReader
 *
 * @author sky
 * @since 2021/6/21 3:45 下午
 */
@Isolated
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
            parts += Part(str.substring(startPos + start.length, endPos), true)
            str = str.substring(endPos + end.length)
        }
        if (str.isNotEmpty()) {
            parts += Part(format(str), false)
        }
        return parts
    }

    private fun format(str: String): String {
        return str.replace("\\$start", start).replace("\\$end", end)
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