package taboolib.module.lang

/**
 * TabooLib
 * taboolib.module.lang.VariableReader
 *
 * @author sky
 * @since 2021/6/21 3:45 下午
 */
class VariableReader(val source: String, val left: Char = '[', val right: Char = ']', val repeat: Int = 1) {

    data class Part(val text: String, val isVariable: Boolean)

    val parts = ArrayList<Part>()

    init {
        var s = 0
        var e = 0
        var text = ""
        var escape = false
        source.forEach {
            when (it) {
                '\\' -> {
                    if (escape) {
                        text += it
                    } else {
                        escape = true
                    }
                }
                left -> {
                    if (escape) {
                        text += it
                    } else {
                        s++
                        if (s == repeat) {
                            parts += Part(text, false)
                            text = ""
                            e = 0
                        }
                    }
                }
                right -> {
                    if (escape) {
                        text += it
                    } else {
                        e++
                        if (s == repeat && e == repeat) {
                            parts += Part(text, true)
                            text = ""
                            s = 0
                            e = 0
                        }
                    }
                }
                else -> {
                    text += it
                }
            }
        }
        parts += Part(text, false)
    }

    override fun toString(): String {
        return "VariableReader(source='$source', left=$left, right=$right, repeat=$repeat, parts=$parts)"
    }
}