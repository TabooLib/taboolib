package taboolib.module.chat.impl

import net.kyori.adventure.key.Key
import net.kyori.adventure.nbt.api.BinaryTagHolder
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.chat.ComponentSerializer
import taboolib.common.UnsupportedVersionException
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.ProxyPlayer
import taboolib.common.platform.function.onlinePlayers
import taboolib.module.chat.*
import java.awt.Color
import java.util.*

class AdventureComponent() : ComponentText {

    constructor(from: Component) : this() {
        left.append(from)
    }

    val left: TextComponent.Builder = Component.text()
    var latest: TextComponent.Builder = Component.text()
    val component: Component
        get() = Component.text().append(left, latest).build()

    override fun toRawMessage(): String {
        return GsonComponentSerializer.gson().serialize(component)
    }

    override fun toLegacyText(): String {
        return LegacyComponentSerializer.legacySection().serialize(component)
    }

    override fun toPlainText(): String {
        return PlainTextComponentSerializer.plainText().serialize(component)
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

    override fun append(text: String, color: Boolean): ComponentText {
        flush()
        if (color) {
            latest += LegacyComponentSerializer.legacySection().deserialize(text)
        } else {
            latest.content(text)
        }
        return this
    }

    override fun append(other: ComponentText): ComponentText {
        other as? AdventureComponent ?: throw UnsupportedVersionException()
        flush()
        latest += other.component
        return this
    }

    override fun appendTranslation(text: String, vararg obj: Any): ComponentText {
        return appendTranslation(text, obj.toList())
    }

    override fun appendTranslation(text: String, obj: List<Any>): ComponentText {
        flush()
        latest += Component.translatable(text, obj.map { if (it is AdventureComponent) it.component else it as? ComponentLike })
        return this
    }

    override fun appendKeybind(key: String): ComponentText {
        flush()
        latest += Component.keybind(key)
        return this
    }

    override fun appendScore(name: String, objective: String): ComponentText {
        flush()
        latest += Component.score(name, objective)
        return this
    }

    override fun appendSelector(selector: String): ComponentText {
        flush()
        latest += Component.selector(selector)
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
        text as? AdventureComponent ?: error("Unsupported component type.")
        latest.hoverEvent(HoverEvent.showText(text.component))
        return this
    }

    override fun hoverItem(id: String, nbt: String): ComponentText {
        latest.hoverEvent(HoverEvent.showItem(Key.key(id), 1, BinaryTagHolder.binaryTagHolder(nbt)))
        return this
    }

    override fun hoverEntity(id: String, type: String?, name: String?): ComponentText {
        return hoverEntity(id, type, name?.let { Components.text(name) })
    }

    override fun hoverEntity(id: String, type: String?, name: ComponentText?): ComponentText {
        val component = if (name is AdventureComponent) name.component else null
        latest.hoverEvent(HoverEvent.showEntity(Key.key(type!!), UUID.fromString(id), component))
        return this
    }

    override fun click(action: ClickAction, value: String): ComponentText {
        when (action) {
            ClickAction.OPEN_URL,
            ClickAction.OPEN_FILE,
            ClickAction.RUN_COMMAND,
            ClickAction.SUGGEST_COMMAND,
            ClickAction.CHANGE_PAGE,
            ClickAction.COPY_TO_CLIPBOARD -> latest.clickEvent(ClickEvent.clickEvent(ClickEvent.Action.valueOf(action.name), value))
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
        latest.insertion(text)
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
        latest.style { it.decoration(TextDecoration.BOLD, true) }
        return this
    }

    override fun unbold(): ComponentText {
        latest.style { it.decoration(TextDecoration.BOLD, false) }
        return this
    }

    override fun italic(): ComponentText {
        latest.style { it.decoration(TextDecoration.ITALIC, true) }
        return this
    }

    override fun unitalic(): ComponentText {
        latest.style { it.decoration(TextDecoration.ITALIC, false) }
        return this
    }

    override fun underline(): ComponentText {
        latest.style { it.decoration(TextDecoration.UNDERLINED, true) }
        return this
    }

    override fun ununderline(): ComponentText {
        latest.style { it.decoration(TextDecoration.UNDERLINED, false) }
        return this
    }

    override fun strikethrough(): ComponentText {
        latest.style { it.decoration(TextDecoration.STRIKETHROUGH, true) }
        return this
    }

    override fun unstrikethrough(): ComponentText {
        latest.style { it.decoration(TextDecoration.STRIKETHROUGH, false) }
        return this
    }

    override fun obfuscated(): ComponentText {
        latest.style { it.decoration(TextDecoration.OBFUSCATED, true) }
        return this
    }

    override fun unobfuscated(): ComponentText {
        latest.style { it.decoration(TextDecoration.OBFUSCATED, false) }
        return this
    }

    override fun font(font: String): ComponentText {
        latest.font(Key.key(font))
        return this
    }

    override fun unfont(): ComponentText {
        latest.font(null)
        return this
    }

    override fun color(color: StandardColors): ComponentText {
        return color(color.toChatColor().color)
    }

    override fun color(color: Color): ComponentText {
        latest.color(TextColor.color(color.rgb))
        return this
    }

    override fun uncolor(): ComponentText {
        latest.color(null)
        return this
    }

    override fun toSpigotObject(): BaseComponent {
        return net.md_5.bungee.api.chat.TextComponent(*ComponentSerializer.parse(toRawMessage()))
    }

    override fun toAdventureObject(): Component {
        return component
    }

    override fun toLegacyRawMessage(): RawMessage {
        return RawMessage(this)
    }

    /** 释放缓冲区 */
    fun flush() {
        left.append(latest)
        latest = Component.text()
    }

    override fun toString(): String {
        return toRawMessage()
    }

    companion object {

        private operator fun TextComponent.Builder.plusAssign(other: Component) {
            append(other)
        }
    }
}