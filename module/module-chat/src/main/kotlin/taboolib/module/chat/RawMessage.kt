package taboolib.module.chat

import net.md_5.bungee.api.chat.*
import net.md_5.bungee.api.chat.hover.content.Item
import net.md_5.bungee.api.chat.hover.content.Text
import net.md_5.bungee.chat.ComponentSerializer
import taboolib.common.Isolated
import taboolib.common.env.RuntimeDependency
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.ProxyPlayer

/**
 * @author sky
 * @since 2018-05-26 14:42json
 */
@RuntimeDependency(
    value = "!net.md-5:bungeecord-chat:1.17", test = "!net.md_5.bungee.api.chat.TextComponent",
    repository = "https://repo2s.ptms.ink/repository/maven-public"
)
@Isolated
@Suppress("DEPRECATION")
open class RawMessage {

    var components = ArrayList<BaseComponent>()
    val componentsLatest = ArrayList<BaseComponent>()

    open fun sendTo(sender: ProxyCommandSender, builder: RawMessage.() -> Unit = {}) {
        builder(this)
        if (sender is ProxyPlayer) {
            sender.sendRawMessage(toRawMessage())
        } else {
            sender.sendMessage(toLegacyText())
        }
    }

    open fun toRawMessage(): String {
        return ComponentSerializer.toString(*componentsAll.toTypedArray())
    }

    open fun toLegacyText(): String {
        return TextComponent.toLegacyText(*componentsAll.toTypedArray())
    }

    open fun toPlainText(): String {
        return TextComponent.toPlainText(*componentsAll.toTypedArray())
    }

    open fun newLine(): RawMessage {
        return append("\n")
    }

    open fun append(text: String): RawMessage {
        new()
        componentsLatest.addAll(TextComponent.fromLegacyText(text))
        return this
    }

    open fun append(json: RawMessage): RawMessage {
        new()
        componentsLatest.addAll(json.componentsAll)
        return this
    }

    open fun hoverText(text: String): RawMessage {
        componentsLatest.forEach {
            try {
                it.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, Text(text))
            } catch (ex: NoClassDefFoundError) {
                it.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(text))
            } catch (ex: NoSuchMethodError) {
                it.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(text))
            }
        }
        return this
    }

    open fun hoverItem(id: String, tag: String = "{}"): RawMessage {
        componentsLatest.forEach {
            try {
                it.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_ITEM, Item(id, 1, ItemTag.ofNbt(tag)))
            } catch (ex: NoClassDefFoundError) {
                it.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_ITEM, ComponentBuilder("{id:\"$id\",Count:1b,tag:$tag}").create())
            } catch (ex: NoSuchMethodError) {
                it.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_ITEM, ComponentBuilder("{id:\"$id\",Count:1b,tag:$tag}").create())
            }
        }
        return this
    }

    open fun insertion(text: String): RawMessage {
        componentsLatest.forEach { it.insertion = text }
        return this
    }

    open fun openURL(url: String): RawMessage {
        componentsLatest.forEach { it.clickEvent = ClickEvent(ClickEvent.Action.OPEN_URL, url) }
        return this
    }

    open fun openFile(file: String): RawMessage {
        componentsLatest.forEach { it.clickEvent = ClickEvent(ClickEvent.Action.OPEN_FILE, file) }
        return this
    }

    open fun runCommand(command: String): RawMessage {
        componentsLatest.forEach { it.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, command) }
        return this
    }

    open fun changePage(page: Int): RawMessage {
        componentsLatest.forEach { it.clickEvent = ClickEvent(ClickEvent.Action.CHANGE_PAGE, page.toString()) }
        return this
    }

    open fun suggestCommand(command: String): RawMessage {
        componentsLatest.forEach { it.clickEvent = ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command) }
        return this
    }

    open fun copyToClipboard(text: String): RawMessage {
        componentsLatest.forEach { it.clickEvent = ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, text) }
        return this
    }

    open val componentsAll: List<BaseComponent>
        get() {
            val components = components.filter { it !is TextComponent || it.text.isNotEmpty() }.toMutableList()
            components.addAll(componentsLatest.filter { it !is TextComponent || it.text.isNotEmpty() })
            return components
        }

    protected fun new() {
        components.addAll(componentsLatest)
        componentsLatest.clear()
    }

    companion object {

        val whitelistTags = arrayOf(
            // 附魔
            "ench",
            // 附魔 1.14
            "Enchantments",
            // 附魔书
            "StoredEnchantments",
            // 展示
            "display",
            // 属性
            "AttributeModifiers",
            // 药水
            "Potion",
            // 特殊药水
            "CustomPotionEffects",
            // 隐藏标签
            "HideFlags",
            // 方块标签
            "BlockEntityTag",
            // Bukkit 自定义标签
            "PublicBukkitValues"
        )
    }
}