package taboolib.module.chat.impl

import taboolib.module.chat.ComponentText
import taboolib.module.chat.Components
import taboolib.module.chat.SimpleComponent
import taboolib.module.chat.TextTransfer

/**
 * 解析一种文本格式：
 * 文本1[特殊文本2](属性=属性值)文本3
 * 例如：
 * 这是一条[红色的[\[可点击\]](command=sb;hover=我是脑瘫)的]测试信息。
 */
class DefaultSimpleComponent(val source: String): SimpleComponent {

    /** 支持转义的字符 */
    val escapes = listOf('[', ']', '(', ')', ';', '=', '\\')

    /** 根文本块 */
    val root = arrayListOf<TextBlock>()

    /** 链接 */
    val links = arrayListOf<String>()

    /** 链接数据 */
    val linkData = hashMapOf<String, DefaultSimpleComponent>()

    init {
        source.lines().forEachIndexed { index, line ->
            // 是否为链接属性
            val linkKey = links.firstOrNull { link -> line.startsWith("@$link=") }
            if (linkKey != null) {
                linkData[linkKey] = DefaultSimpleComponent(line.substring("@$linkKey=".length))
                return@forEachIndexed
            }
            // 解析文本
            val newBlock = TextBlock(0)
            if (index != 0) {
                root += TextBlock.NewLine()
            }
            root += newBlock
            find(newBlock, line, 0)
        }
    }

    /** 构建为 RawMessage */
    override fun build(transfer: TextTransfer.() -> Unit): ComponentText {
        return build(TextTransfer(this).also(transfer))
    }

    /** 构建为 RawMessage */
    fun build(transfer: TextTransfer): ComponentText {
        val rawMessage = Components.empty()
        root.forEach { block ->
            if (block is TextBlock.NewLine) {
                rawMessage.newLine()
            } else {
                rawMessage.append(block.build(transfer))
            }
        }
        return rawMessage
    }

    /** 创建同级文本块 */
    private fun TextBlock.createSibling(): TextBlock {
        val newBlock = TextBlock(level, parent = parent)
        if (newBlock.level == 0) {
            root += newBlock
        } else {
            parent!!.subBlocks += newBlock
        }
        return newBlock
    }

    /** 查找文本块 */
    private fun find(block: TextBlock, source: String, start: Int): Int {
        var i = start
        while (i < source.length) {
            val c = source[i]
            if (c == '\\' && i + 1 < source.length && escapes.contains(source[i + 1])) {
                block += source[i + 1]
                i += 2
            } else if (c == '[') {
                return find(block.createSibling(), source, find(block.createSubBlock(), source, i + 1))
            } else if (c == ']') {
                // 检查是否有属性
                if (i + 1 < source.length && source[i + 1] == '(') {
                    val end = findToRight(source, ')', i + 2)
                    if (end == -1) {
                        error("Property not closed.")
                    }
                    // 解析属性
                    val props = split(';', source.substring(i + 2, end)).map { split('=', it) }.associate { args ->
                        format(args[0]) to if (args.size == 2) {
                            val value = format(args[1])
                            if (value.isNotEmpty() && value[0] == '@') {
                                // 注册链接
                                links += value.substring(1)
                                PropertyValue.Link(value.substring(1))
                            } else {
                                PropertyValue.Text(value)
                            }
                        } else {
                            null
                        }
                    }
                    // 赋予属性
                    block.getSiblingBlocks().forEach { it.properties += props }
                    i = end
                }
                return i + 1
            } else {
                block += c
                i++
            }
        }
        return i
    }

    /**
     * 向右查找最近的符号
     */
    private fun findToRight(source: String, char: Char, start: Int = 0): Int {
        var i = start
        while (i < source.length) {
            if (source[i] == char) {
                if (i == 0 || source[i - 1] != '\\') {
                    return i
                }
            }
            i++
        }
        return -1
    }

    /**
     * 分割字符串并支持转义
     */
    private fun split(delimiter: Char, source: String): List<String> {
        val arr = arrayListOf<String>()
        var i = 0
        var start = 0
        while (i < source.length) {
            if (source[i] == delimiter) {
                if (i == 0 || source[i - 1] != '\\') {
                    arr += source.substring(start, i).trim()
                    start = i + 1
                }
            }
            i++
        }
        arr += source.substring(start, i).trim()
        return arr
    }

    /**
     * 简化格式化方法，不再进行长文本转义处理
     */
    private fun format(str: String): String {
        return buildString {
            var i = 0
            while (i < str.length) {
                if (str[i] == '\\') {
                    // 仅处理支持转义的字符
                    if (i + 1 < str.length && escapes.contains(str[i + 1])) {
                        append(str[i + 1])
                        i += 2
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
}