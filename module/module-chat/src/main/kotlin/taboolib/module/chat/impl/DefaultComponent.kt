package taboolib.module.chat.impl

import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.*
import net.md_5.bungee.api.chat.hover.content.Entity
import net.md_5.bungee.api.chat.hover.content.Item
import net.md_5.bungee.api.chat.hover.content.Text
import net.md_5.bungee.chat.ComponentSerializer
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.ProxyPlayer
import taboolib.common.platform.function.onlinePlayers
import taboolib.module.chat.*
import java.awt.Color

/**
 * TabooLib
 * taboolib.module.chat.impl.DefaultComponent
 *
 * @author 坏黑
 * @since 2023/2/9 20:36
 */
class DefaultComponent() : ComponentText {

    constructor(from: List<BaseComponent>) : this() {
        left.addAll(from)
    }

    private val left = arrayListOf<BaseComponent>()
    private val latest = arrayListOf<BaseComponent>()
    private val component: BaseComponent
        get() = when {
            left.isEmpty() && latest.size == 1 -> latest[0]
            latest.isEmpty() && left.size == 1 -> left[0]
            else -> TextComponent(*(left + latest).toTypedArray())
        }

    init {
        color(StandardColors.RESET)
    }

    override fun toRawMessage(): String {
        return ComponentSerializer.toString(component)
    }

    override fun toLegacyText(): String {
        return ComponentToString.toLegacyString(*(left + latest).toTypedArray())
    }

    override fun toPlainText(): String {
        return TextComponent.toPlainText(*(left + latest).toTypedArray())
    }

    override fun broadcast() {
        onlinePlayers().forEach { sendTo(it) }
    }

    override fun sendTo(sender: ProxyCommandSender) {
        if (sender is ProxyPlayer) {
            sender.sendRawMessage(toRawMessage())
        } else {
            sender.sendMessage(toLegacyText())
        }
    }

    override fun newLine(): ComponentText {
        return append("\n")
    }

    override fun plusAssign(text: String) {
        append(text)
    }

    override fun plusAssign(other: ComponentText) {
        append(other)
    }

    override fun append(text: String): ComponentText {
        flush()
        latest += try {
            TextComponent.fromLegacyText(text, ChatColor.RESET)
        } catch (_: NoSuchMethodError) {
            TextComponent.fromLegacyText("${ChatColor.RESET}$text")
        }
        return this
    }

    override fun append(other: ComponentText): ComponentText {
        other as? DefaultComponent ?: error("Unsupported component type.")
        flush()
        latest += other.component
        return this
    }

    override fun appendTranslation(text: String, vararg obj: Any): ComponentText {
        return appendTranslation(text, obj.toList())
    }

    override fun appendTranslation(text: String, obj: List<Any>): ComponentText {
        flush()
        latest += TranslatableComponent(text, obj.map { if (it is DefaultComponent) it.component else it })
        return this
    }

    override fun appendKeybind(key: String): ComponentText {
        flush()
        latest += KeybindComponent(key)
        return this
    }

    override fun appendScore(name: String, objective: String): ComponentText {
        flush()
        latest += ScoreComponent(name, objective)
        return this
    }

    override fun appendSelector(selector: String): ComponentText {
        flush()
        latest += SelectorComponent(selector)
        return this
    }

    override fun hoverText(text: String): ComponentText {
        return hoverText(ComponentText.of(text))
    }

    override fun hoverText(text: List<String>): ComponentText {
        val component = ComponentText.empty()
        text.forEachIndexed { index, s ->
            component.append(s)
            if (index != text.size - 1) {
                component.newLine()
            }
        }
        return hoverText(component)
    }

    override fun hoverText(text: ComponentText): ComponentText {
        text as? DefaultComponent ?: error("Unsupported component type.")
        try {
            latest.forEach { it.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, Text(arrayOf(text.component))) }
        } catch (_: NoClassDefFoundError) {
            latest.forEach { it.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, arrayOf(text.component)) }
        } catch (_: NoSuchMethodError) {
            latest.forEach { it.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, arrayOf(text.component)) }
        }
        return this
    }

    override fun hoverItem(id: String, nbt: String): ComponentText {
        try {
            latest.forEach { it.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_ITEM, Item(id, 1, ItemTag.ofNbt(nbt))) }
        } catch (_: NoClassDefFoundError) {
            latest.forEach { it.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_ITEM, ComponentBuilder("{id:\"$id\",Count:1b,tag:$nbt}").create()) }
        } catch (_: NoSuchMethodError) {
            latest.forEach { it.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_ITEM, ComponentBuilder("{id:\"$id\",Count:1b,tag:$nbt}").create()) }
        }
        return this
    }

    override fun hoverEntity(id: String, type: String?, name: String?): ComponentText {
        try {
            val component = if (name != null) TextComponent(name) else null
            latest.forEach { it.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_ENTITY, Entity(type, id, component)) }
        } catch (_: NoClassDefFoundError) {
            TODO("Unsupported hover entity for this version.")
        }
        return this
    }

    override fun hoverEntity(id: String, type: String?, name: ComponentText?): ComponentText {
        try {
            val component = if (name is DefaultComponent) name.component else null
            latest.forEach { it.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_ENTITY, Entity(type, id, component)) }
        } catch (_: NoClassDefFoundError) {
            TODO("Unsupported hover entity for this version.")
        }
        return this
    }

    override fun click(action: ClickAction, value: String): ComponentText {
        when (action) {
            ClickAction.OPEN_URL,
            ClickAction.OPEN_FILE,
            ClickAction.RUN_COMMAND,
            ClickAction.SUGGEST_COMMAND,
            ClickAction.CHANGE_PAGE,
            ClickAction.COPY_TO_CLIPBOARD -> latest.forEach { it.clickEvent = ClickEvent(ClickEvent.Action.valueOf(action.name), value) }
            // 插入文本
            ClickAction.INSERTION -> clickInsertText(value)
        }
        return this
    }

    override fun clickOpenURL(url: String): ComponentText {
        return click(ClickAction.OPEN_URL, url)
    }

    override fun clickOpenFile(file: String): ComponentText {
        return click(ClickAction.OPEN_FILE, file)
    }

    override fun clickRunCommand(command: String): ComponentText {
        return click(ClickAction.RUN_COMMAND, command)
    }

    override fun clickSuggestCommand(command: String): ComponentText {
        return click(ClickAction.SUGGEST_COMMAND, command)
    }

    override fun clickChangePage(page: Int): ComponentText {
        return click(ClickAction.CHANGE_PAGE, page.toString())
    }

    override fun clickCopyToClipboard(text: String): ComponentText {
        return click(ClickAction.COPY_TO_CLIPBOARD, text)
    }

    override fun clickInsertText(text: String): ComponentText {
        latest.forEach { it.insertion = text }
        return this
    }

    override fun decoration(decoration: Decoration): ComponentText {
        when (decoration) {
            Decoration.BOLD -> bold()
            Decoration.ITALIC -> italic()
            Decoration.UNDERLINE -> underline()
            Decoration.STRIKETHROUGH -> strikethrough()
            Decoration.OBFUSCATED -> obfuscated()
        }
        return this
    }

    override fun undecoration(decoration: Decoration): ComponentText {
        when (decoration) {
            Decoration.BOLD -> unbold()
            Decoration.ITALIC -> unitalic()
            Decoration.UNDERLINE -> ununderline()
            Decoration.STRIKETHROUGH -> unstrikethrough()
            Decoration.OBFUSCATED -> unobfuscated()
        }
        return this
    }

    override fun undecoration(): ComponentText {
        unbold()
        unitalic()
        ununderline()
        unstrikethrough()
        unobfuscated()
        return this
    }

    override fun bold(): ComponentText {
        latest.forEach { it.isBold = true }
        return this
    }

    override fun unbold(): ComponentText {
        latest.forEach { it.isBold = false }
        return this
    }

    override fun italic(): ComponentText {
        latest.forEach { it.isItalic = true }
        return this
    }

    override fun unitalic(): ComponentText {
        latest.forEach { it.isItalic = false }
        return this
    }

    override fun underline(): ComponentText {
        latest.forEach { it.isUnderlined = true }
        return this
    }

    override fun ununderline(): ComponentText {
        latest.forEach { it.isUnderlined = false }
        return this
    }

    override fun strikethrough(): ComponentText {
        latest.forEach { it.isStrikethrough = true }
        return this
    }

    override fun unstrikethrough(): ComponentText {
        latest.forEach { it.isStrikethrough = false }
        return this
    }

    override fun obfuscated(): ComponentText {
        latest.forEach { it.isObfuscated = true }
        return this
    }

    override fun unobfuscated(): ComponentText {
        latest.forEach { it.isObfuscated = false }
        return this
    }

    override fun font(font: String): ComponentText {
        latest.forEach { it.font = font }
        return this
    }

    override fun unfont(): ComponentText {
        latest.forEach { it.font = null }
        return this
    }

    override fun color(color: StandardColors): ComponentText {
        latest.forEach { it.color = color.toChatColor() }
        return this
    }

    override fun color(color: Color): ComponentText {
        latest.forEach { it.color = ChatColor.of(color) }
        return this
    }

    override fun uncolor(): ComponentText {
        latest.forEach { it.color = null }
        return this
    }

    override fun toSpigotObject(): BaseComponent {
        return component
    }

    /** 释放缓冲区 */
    fun flush() {
        left.addAll(latest)
        latest.clear()
    }

    override fun toString(): String {
        return toRawMessage()
    }
}