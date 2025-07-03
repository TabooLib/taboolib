package taboolib.module.chat

import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.md_5.bungee.chat.ComponentSerializer
import taboolib.common.Inject
import taboolib.common.env.RuntimeDependency
import taboolib.module.chat.impl.AdventureComponent
import taboolib.module.chat.impl.DefaultComponent
import taboolib.module.chat.impl.DefaultSimpleComponent
import taboolib.module.chat.impl.ErrorSimpleComponent

/**
 * TabooLib
 * taboolib.module.chat.Components
 *
 * @author 坏黑
 * @since 2023/2/9 20:16
 */
@Inject
@RuntimeDependency(
    value = "!net.md-5:bungeecord-chat:1.21-R0.2",
    test = "!net.md_5.bungee.api.chat.TextComponent",
    // relocate = ["!net.md_5.bungee", "!net.md_5.bungee117"],
    // repository = "https://repo2s.ptms.ink/repository/releases"
)
object Components {

    /** 使用adventure作为底层 */
    var useAdventure = false

    /** 创建空白块 */
    fun empty(): ComponentText {
        return if (useAdventure) {
            AdventureComponent()
        } else {
            DefaultComponent()
        }
    }

    /** 创建文本块 */
    @JvmOverloads
    fun text(text: String, color: Boolean = true): ComponentText = empty().append(text, color)

    /** 创建分数文本块 */
    fun score(name: String, objective: String): ComponentText = empty().appendScore(name, objective)

    /** 创建按键文本块 */
    fun keybind(key: String): ComponentText = empty().appendKeybind(key)

    /** 创建选择器文本块 */
    fun selector(selector: String): ComponentText = empty().appendSelector(selector)

    /** 创建翻译文本块 */
    fun translation(text: String, vararg obj: Any): ComponentText = empty().appendTranslation(text, *obj)

    /** 创建翻译文本块 */
    fun translation(text: String, obj: List<Any>): ComponentText = empty().appendTranslation(text, obj)

    /** 从原始信息中读取 */
    fun parseRaw(text: String): ComponentText {
        return if (useAdventure) {
            AdventureComponent(GsonComponentSerializer.gson().deserialize(text))
        } else {
            DefaultComponent(ComponentSerializer.parse(text).toList())
        }
    }

    /**
     * 解析一种文本格式：
     * 文本1[特殊文本2](属性=属性值)文本3
     * 例如：
     * 这是一条[红色的[\[可点击\]](command=sb;hover=我是脑瘫)的]测试信息。
     */
    fun parseSimple(text: String): SimpleComponent {
        return try {
            DefaultSimpleComponent(text)
        } catch (ex: Throwable) {
            ErrorSimpleComponent(ex)
        }
    }

    /** 解析 TabooLib SimpleComponent 并转换为 Raw */
    fun parseSimpleToRaw(text: String, transfer: TextTransfer.() -> Unit = {}) = parseSimple(text).buildToRaw(transfer)

    /** 解析 TabooLib SimpleComponent 并转换为 Raw */
    fun parseSimpleToRaw(text: List<String>, transfer: TextTransfer.() -> Unit = {}) = text.map { parseSimple(it).buildToRaw(transfer) }

    /** 解析 TabooLib SimpleComponent 并转换为 RawMessage */
    fun parseSimpleToLegacyRaw(text: String, transfer: TextTransfer.() -> Unit = {}) = parseSimple(text).buildColored(transfer).toLegacyRawMessage()

    /** 解析 TabooLib SimpleComponent 并转换为 RawMessage */
    fun parseSimpleToLegacyRaw(text: List<String>, transfer: TextTransfer.() -> Unit = {}) = text.map { parseSimple(it).buildColored(transfer).toLegacyRawMessage() }
}