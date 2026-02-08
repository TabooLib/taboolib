package taboolib.module.chat.impl

import net.md_5.bungee.api.ChatColor
import taboolib.common.util.t
import taboolib.module.chat.*
import java.awt.Color

/** 文本块 */
open class TextBlock(val level: Int, val properties: MutableMap<String, PropertyValue?> = hashMapOf(), var parent: TextBlock? = null) {

    /** 换行文本块 */
    class NewLine : TextBlock(-1) {

        override fun toString(): String {
            return "NL"
        }
    }

    /** 子文本块 */
    val subBlocks = mutableListOf<TextBlock>()

    /** 文本内容 */
    private val builder = StringBuilder()

    /** 文本内容 */
    private val text: String
        get() = builder.toString()

    /** 添加文本 */
    operator fun plusAssign(char: Char) {
        builder.append(char)
    }

    /** 创建子文本块 */
    fun createSubBlock(): TextBlock {
        return TextBlock(level + 1, parent = this).also { subBlocks += it }
    }

    /** 获取同级文本块 */
    fun getSiblingBlocks(): List<TextBlock> {
        return parent?.subBlocks ?: emptyList()
    }

    /** 打印结构 */
    override fun toString(): String {
        val arr = arrayListOf<String>()
        arr += "L:$level | text=\"$text\" props=$properties"
        subBlocks.forEach { arr += it.toString() }
        return arr.joinToString("\n")
    }

    /** 构建 RawMessage */
    fun build(transfer: TextTransfer): ComponentText {
        val rawMessage = Components.empty()
        val newText = transfer(text)
        // 文本类型
        when {
            // 快捷键
            properties.containsKey("keybind") || properties.containsKey("key") -> rawMessage.appendKeybind(newText)
            // 选择器
            properties.containsKey("selector") || properties.containsKey("select") -> rawMessage.appendSelector(newText)
            // 分数
            properties["score"] != null -> {
                val obj = properties["objective", "obj"] ?: error(
                    """
                        缺少 "objective" 参数。
                        Missing objective for score.
                    """.t()
                )
                rawMessage.appendScore(newText, transfer(obj))
            }
            // 渐变
            properties["gradient", "g"] != null -> {
                val color = properties["gradient", "g"] ?: error(
                    """
                        缺少 "gradient" 参数。
                        Missing color for gradient.
                    """.t()
                )
                rawMessage.append(newText.toGradientColor(transfer(color).split(',').map { it.parseToHexColor() }))
            }
            // 语言文件
            // 这玩意儿还是算了吧，泛用性真不高
            properties.containsKey("translate") || properties.containsKey("trans") -> {
                // 语言文件参数
                // 支持格式: arg0=bar;arg1=foo;args=foo,bar
                val args = arrayListOf<TransArgument>()
                properties.forEach { (k, v) ->
                    if (k.startsWith("arg") && v != null) {
                        val order = k.substring(3).toInt()
                        args += if (v is PropertyValue.Link) TransArgument(v.getValue(transfer), order) else TransArgument(v, order)
                    }
                }
                val offset = args.size
                if (properties["args"] != null) {
                    properties["args"]!!.toString().split(',').forEachIndexed { index, s ->
                        args += TransArgument(s, index + offset)
                    }
                }
                rawMessage.appendTranslation(newText, args.sortedBy { it.order }.map { it.value })
            }
            // 标准文本
            else -> rawMessage.append(newText)
        }
        // 属性
        properties.forEach { (key, value) ->
            when (key) {
                "s" -> rawMessage.strikethrough()
                "u" -> rawMessage.underline()
                "i", "italic" -> rawMessage.italic()
                "b", "bold" -> rawMessage.bold()
                "o", "obf" -> rawMessage.obfuscated()
                "r", "reset" -> rawMessage.undecoration().uncolor()
                "br", "nl", "newline" -> rawMessage.newLine()
                "f", "font" -> rawMessage.font(transfer(value))
                "url" -> rawMessage.clickOpenURL(transfer(value))
                "file" -> rawMessage.clickOpenFile(transfer(value))
                "cmd", "command" -> rawMessage.clickRunCommand(transfer(value))
                "suggest" -> rawMessage.clickSuggestCommand(transfer(value))
                "page" -> rawMessage.clickChangePage(transfer(value).toInt())
                "copy" -> rawMessage.clickCopyToClipboard(transfer(value))
                "insert", "insertion" -> rawMessage.clickInsertText(transfer(value))
                "h", "hover" -> {
                    if (value is PropertyValue.Link) {
                        rawMessage.hoverText(value.getValue(transfer))
                    } else {
                        val content = transfer(value)
                        when {
                            content.contains("<br>") -> rawMessage.hoverText(content.split("<br>"))
                            content.contains("||") -> rawMessage.hoverText(content.split("||"))
                            else -> rawMessage.hoverText(content)
                        }
                    }
                }
                // 颜色
                "c", "color" -> {
                    val color = value?.toString() ?: error(
                        """
                            缺少颜色参数。
                            Missing color.
                        """.t()
                    )
                    if (color.length == 1) {
                        rawMessage.color(ChatColor.getByChar(color[0])?.color ?: error(
                            """
                                无效的颜色代码: ${color[0]}
                                Invalid color code: ${color[0]}
                            """.t()
                        ))
                    } else {
                        rawMessage.color(Color(color.parseToHexColor()))
                    }
                }
                // 自定义添加 Translation 值
                "translation" -> {
                    rawMessage.appendTranslation(transfer(value))
                }
            }
        }
        subBlocks.forEach { rawMessage.append(it.build(transfer)) }
        return rawMessage
    }

    /** 通过多个 key 获取属性 */
    private operator fun <K, V> MutableMap<K, V>.get(vararg key: K): V? {
        key.forEach { if (containsKey(it)) return get(it) }
        return null
    }
}
