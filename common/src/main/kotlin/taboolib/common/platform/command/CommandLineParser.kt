package taboolib.common.platform.command

/**
 * TabooLib
 * taboolib.common.platform.command.CommandLineParser
 *
 * @author 坏黑
 * @since 2023/2/27 09:28
 */
class CommandLineParser(val line: String) {

    /** 支持转义的字符 */
    val escapes = arrayOf('\'', '\"', '=', '-', ':', '\\')

    /** 选项 */
    val options = mutableMapOf<String, String>()

    /** 参数 */
    val args = mutableListOf<String>()

    /** 当前文本块 */
    val value = StringBuilder()

    /** 引用 */
    var quote = false

    /** 选项 */
    var option = false

    /** 选项名称 */
    val optionName = StringBuilder()

    /**
     * 识别一种命令格式：[选项] [参数]
     * 例如：-a -b 1 2 3
     * 选项：{a=, b=}
     * 参数：[1, 2, 3]
     *
     * 选项可以使用等号赋值，例如：-a=1 -b=2 1 2 3
     * 此时选项为：{a=1, b=2}
     * 此时参数为：[1, 2, 3]
     *
     * 参数可以使用双引号或单引号包裹，例如：/test -a='N M S L' "1 2 3"
     * 此时选项为：{a=N M S L}
     * 此时参数为：[1 2 3]
     *
     * 引号可以使用反斜杠转义，例如：/test -a='N M S L' \"1 2 3\"
     * 此时选项为：{a=N M S L}
     * 此时参数为：["1, 2, 3"]
     */
    fun parse(): CommandLineParser {
        var i = 0
        val len = line.length
        while (i < len) {
            val c = line[i]
            when {
                // 转义字符
                c == '\\' && i + 1 < line.length && escapes.contains(line[i + 1]) -> {
                    value += line[i + 1]
                    i += 2
                }
                // 引号
                c == '\'' || c == '\"' -> {
                    // 引号结束
                    if (quote) {
                        close()
                    }
                    quote = !quote
                    i++
                }
                // 空格
                c == ' ' && !quote -> {
                    close()
                    i++
                }
                // 选项
                c == '-' && !quote -> {
                    option = true
                    i++
                }
                // 选项名称
                option && c == '=' || c == ':' -> {
                    optionName += value
                    value.clear()
                    i++
                }
                // 其他文本
                else -> {
                    value += c
                    i++
                }
            }
        }
        close()
        return this
    }

    /** 结束 */
    fun close() {
        // 先关闭选项，否则会导致选项名称丢失
        closeOption()
        // 再关闭参数
        closeValue()
    }

    /** 结束参数 */
    fun closeValue() {
        if (value.isNotEmpty()) {
            args += value.toString()
            value.clear()
        }
    }

    /** 结束选项 */
    fun closeOption() {
        if (option) {
            if (optionName.isNotEmpty()) {
                options[optionName.toString()] = value.toString()
            } else {
                options[value.toString()] = ""
            }
            optionName.clear()
            value.clear()
            option = false
        }
    }

    operator fun StringBuilder.plusAssign(c: Char) {
        this.append(c)
    }

    operator fun StringBuilder.plusAssign(s: String) {
        this.append(s)
    }

    operator fun StringBuilder.plusAssign(s: StringBuilder) {
        this.append(s)
    }
}