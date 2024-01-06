package taboolib.module.lang

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.util.VariableReader
import taboolib.common.util.asList
import taboolib.common.util.replaceWithOrder
import taboolib.module.chat.Components
import taboolib.module.chat.colored
import taboolib.module.chat.parseToHexColor
import taboolib.module.chat.toGradientColor

/**
 * TabooLib
 * taboolib.module.lang.TypeJson
 *
 * @author sky
 * @since 2021/6/20 10:55 下午
 */
@Suppress("SpellCheckingInspection")
class TypeJson : Type {

    var text: List<String>? = null
    var jsonArgs = ArrayList<Map<String, Any>>()

    override fun init(source: Map<String, Any>) {
        text = source["text"]?.asList()
        try {
            jsonArgs.addAll((source["args"] as List<*>).map { (it as Map<*, *>).map { (k, v) -> k.toString() to v!! }.toMap() })
        } catch (_: ClassCastException) {
        }
    }

    fun formated(string: String, sender: ProxyCommandSender, vararg args: Any): String {
        return string.translate(sender, *args).replaceWithOrder(*args).colored()
    }

    override fun send(sender: ProxyCommandSender, vararg args: Any) {
        /** 转换文本 */
        val rawMessage = Components.empty()
        var i = 0

        rawMessage.apply {
            text?.forEachIndexed { index, line ->
                // 加载变量
                parser.readToFlatten(line).forEach { part ->
                    // 获取文本块类型
                    val extra = if (part.isVariable) jsonArgs.getOrNull(i++) else emptyMap()
                    if (extra == null) {
                        append("§c[RAW OPTION NOT FOUND]")
                        return@forEach
                    }
                    // 显示文字
                    val showText = formated(part.text, sender, *args)
                    val showType = formated(extra["type"].toString(), sender, *args)
                    when {
                        // 快捷键
                        showType == "keybind" -> appendKeybind(showText)
                        // 选择器
                        showType == "selector" -> appendSelector(showText)
                        // 语言
                        // text: '[commands.drop.success.single]'
                        // args:
                        // - type: translate:1:Stone
                        showType == "translate" -> appendTranslation(showText, *showType.substringAfter(':').split(':').toTypedArray())
                        // 分数
                        showType == "score" -> appendScore(showText.substringBefore(':'), showText.substringAfter(':'))
                        // 渐变颜色文本
                        // text: 'Woo: [||||||||||||||||||||||||]'
                        // args:
                        // - type: gradient:#ff0000:#00ff00:#0000ff:#ff0000
                        showType.startsWith("gradient") -> {
                            append(showText.toGradientColor(showType.substringAfter(':').split(':').map { it.parseToHexColor() }))
                        }
                        // 标准
                        else -> append(showText.colored())
                    }
                    // 附加信息
                    if (extra.containsKey("hover")) {
                        hoverText(formated(extra["hover"].toString(), sender, *args))
                    }
                    if (extra.containsKey("command")) {
                        clickRunCommand(formated(extra["command"].toString(), sender, *args))
                    }
                    if (extra.containsKey("suggest")) {
                        clickSuggestCommand(formated(extra["suggest"].toString(), sender, *args))
                    }
                    if (extra.containsKey("insertion")) {
                        clickInsertText(formated(extra["insertion"].toString(), sender, *args))
                    }
                    if (extra.containsKey("copy")) {
                        clickCopyToClipboard(formated(extra["copy"].toString(), sender, *args))
                    }
                    if (extra.containsKey("file")) {
                        clickOpenFile(formated(extra["file"].toString(), sender, *args))
                    }
                    if (extra.containsKey("url")) {
                        clickOpenURL(formated(extra["url"].toString(), sender, *args))
                    }
                    if (extra.containsKey("font")) {
                        font(formated(extra["font"].toString(), sender, *args))
                    }
                }
                if (index + 1 < text!!.size) {
                    newLine()
                }
            }
        }
        rawMessage.sendTo(sender)
    }

    companion object {

        private val parser = VariableReader("[", "]")
    }
}
