package taboolib.module.chat

import net.md_5.bungee.api.chat.BaseComponent
import java.awt.Color

/**
 * TabooLib
 * taboolib.module.chat.ComponentText
 *
 * @author 坏黑
 * @since 2023/2/9 20:17
 */
@Suppress("SpellCheckingInspection")
interface ComponentText : Source {

    /** 换行 */
    fun newLine(): ComponentText

    /** 添加文本块 */
    operator fun plusAssign(text: String)

    /** 追加另一个 [ComponentText] */
    operator fun plusAssign(other: ComponentText)

    /** 添加文本块 */
    fun append(text: String): ComponentText

    /** 追加另一个 [ComponentText] */
    fun append(other: ComponentText): ComponentText

    /** 添加翻译文本块 */
    fun appendTranslation(text: String, vararg obj: Any): ComponentText

    /** 添加翻译文本块 */
    fun appendTranslation(text: String, obj: List<Any>): ComponentText

    /** 添加按键文本块 */
    fun appendKeybind(key: String): ComponentText

    /** 添加分数文本块 */
    fun appendScore(name: String, objective: String): ComponentText

    /** 添加选择器文本块 */
    fun appendSelector(selector: String): ComponentText

    /** 显示文本 */
    fun hoverText(text: String): ComponentText

    /** 显示多行文本 */
    fun hoverText(text: List<String>): ComponentText

    /** 显示 [ComponentText] */
    fun hoverText(text: ComponentText): ComponentText

    /** 显示物品 */
    fun hoverItem(id: String, nbt: String = "{}"): ComponentText

    /** 显示实体 */
    fun hoverEntity(id: String, type: String? = null, name: String? = null): ComponentText

    /** 显示实体 */
    fun hoverEntity(id: String, type: String? = null, name: ComponentText? = null): ComponentText

    /** 交互行为 */
    fun click(action: ClickAction, value: String): ComponentText

    /** 打开链接 */
    fun clickOpenURL(url: String): ComponentText

    /** 打开文件 */
    fun clickOpenFile(file: String): ComponentText

    /** 执行命令 */
    fun clickRunCommand(command: String): ComponentText

    /** 建议命令 */
    fun clickSuggestCommand(command: String): ComponentText

    /** 切换页码 */
    fun clickChangePage(page: Int): ComponentText

    /** 复制文本 */
    fun clickCopyToClipboard(text: String): ComponentText

    /** 插入文本 */
    fun clickInsertText(text: String): ComponentText

    /** 添加装饰 */
    fun decoration(decoration: Decoration): ComponentText

    /** 移除装饰 */
    fun undecoration(decoration: Decoration): ComponentText

    /** 移除所有装饰 */
    fun undecoration(): ComponentText

    /** 加粗 */
    fun bold(): ComponentText

    /** 移除加粗 */
    fun unbold(): ComponentText

    /** 斜体 */
    fun italic(): ComponentText

    /** 移除斜体 */
    fun unitalic(): ComponentText

    /** 下划线 */
    fun underline(): ComponentText

    /** 移除下划线 */
    fun ununderline(): ComponentText

    /** 删除线 */
    fun strikethrough(): ComponentText

    /** 移除删除线 */
    fun unstrikethrough(): ComponentText

    /** 模糊 */
    fun obfuscated(): ComponentText

    /** 移除模糊 */
    fun unobfuscated(): ComponentText

    /** 添加字体 */
    fun font(font: String): ComponentText

    /** 移除字体 */
    fun unfont(): ComponentText

    /** 添加颜色 */
    fun color(color: StandardColors): ComponentText

    /** 添加颜色 */
    fun color(color: Color): ComponentText

    /** 移除颜色 */
    fun uncolor(): ComponentText

    companion object {

        fun empty(): ComponentText {
            return Components.empty()
        }

        fun of(text: String): ComponentText {
            return Components.text(text)
        }

        fun raw(raw: String): ComponentText {
            return Components.parseRaw(raw)
        }
    }
}