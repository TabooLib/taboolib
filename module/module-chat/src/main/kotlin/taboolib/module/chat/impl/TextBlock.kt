package taboolib.module.chat.impl

import net.md_5.bungee.api.ChatColor
import taboolib.module.chat.*
import java.awt.Color

/** 文本块 */
@Suppress("SpellCheckingInspection")
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
                val obj = properties["objective", "obj"] ?: error("Missing objective for score.")
                rawMessage.appendScore(newText, transfer(obj))
            }
            // 渐变
            properties["gradient", "g"] != null -> {
                val color = properties["gradient", "g"] ?: error("Missing color for gradient.")
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
                "i" -> rawMessage.italic()
                "b" -> rawMessage.bold()
                "o" -> rawMessage.obfuscated()
                "newline", "br" -> rawMessage.newLine()
                "font" -> rawMessage.font(transfer(value))
                "url" -> rawMessage.clickOpenURL(transfer(value))
                "file" -> rawMessage.clickOpenFile(transfer(value))
                "command", "cmd" -> rawMessage.clickRunCommand(transfer(value))
                "suggest" -> rawMessage.clickSuggestCommand(transfer(value))
                "page" -> rawMessage.clickChangePage(transfer(value).toInt())
                "copy" -> rawMessage.clickCopyToClipboard(transfer(value))
                "insertion", "insert" -> rawMessage.clickInsertText(transfer(value))
                "hover", "h" -> {
                    if (value is PropertyValue.Link) {
                        rawMessage.hoverText(value.getValue(transfer))
                    } else {
                        val content = transfer(value)
                        if (content.contains("<br>")) {
                            rawMessage.hoverText(content.split("<br>"))
                        } else {
                            rawMessage.hoverText(content)
                        }
                    }
                }
                "color", "c" -> {
                    val color = value?.toString() ?: error("Missing color.")
                    if (color.length == 1) {
                        rawMessage.color(ChatColor.getByChar(color[0])?.color ?: error("Invalid color code: ${color[0]}"))
                    } else {
                        rawMessage.color(Color(color.parseToHexColor()))
                    }
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
