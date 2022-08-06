package taboolib.module.chat

import net.md_5.bungee.api.chat.*
import net.md_5.bungee.api.chat.hover.content.Item
import net.md_5.bungee.api.chat.hover.content.Text
import net.md_5.bungee.chat.ComponentSerializer
import taboolib.common.Isolated
import taboolib.common.env.RuntimeDependency
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.ProxyPlayer
import taboolib.common.platform.function.onlinePlayers

/**
 * @author sky
 * @since 2018-05-26 14:42json
 */
@RuntimeDependency(
    value = "!net.md-5:bungeecord-chat:1.17", test = "!net.md_5.bungee.api.chat.TextComponent", repository = "https://repo2s.ptms.ink/repository/releases"
)
@Isolated
class TellrawJson {

    var components = ArrayList<BaseComponent>()
    val componentsLatest = ArrayList<BaseComponent>()

    fun sendTo(sender: ProxyCommandSender, builder: TellrawJson.() -> Unit = {}) {
        builder(this)
        if (sender is ProxyPlayer) {
            sender.sendRawMessage(toRawMessage())
        } else {
            sender.sendMessage(toLegacyText())
        }
    }

    fun broadcast(builder: TellrawJson.() -> Unit = {}) {
        builder(this)
        onlinePlayers().forEach { p -> sendTo(p) }
    }

    fun toRawMessage(): String {
        return ComponentSerializer.toString(*componentsAll.toTypedArray())
    }

    fun toLegacyText(): String {
        return TextComponent.toLegacyText(*componentsAll.toTypedArray())
    }

    fun toPlainText(): String {
        return TextComponent.toPlainText(*componentsAll.toTypedArray())
    }

    fun newLine(): TellrawJson {
        return append("\n")
    }

    fun append(text: String): TellrawJson {
        new()
        componentsLatest.addAll(TextComponent.fromLegacyText(text))
        return this
    }

    fun appendTranslatable(node: String, vararg obj: Any): TellrawJson {
        new()
        componentsLatest.add(TranslatableComponent(node, obj))
        return this
    }

    @Suppress("SpellCheckingInspection")
    fun appendKeybind(keybind: String): TellrawJson {
        new()
        componentsLatest.add(KeybindComponent(keybind))
        return this
    }

    fun appendScore(name: String, objective: String): TellrawJson {
        new()
        componentsLatest.add(ScoreComponent(name, objective))
        return this
    }

    fun appendSelector(selector: String): TellrawJson {
        new()
        componentsLatest.add(SelectorComponent(selector))
        return this
    }

    fun append(json: TellrawJson): TellrawJson {
        new()
        componentsLatest.addAll(json.componentsAll)
        return this
    }

    fun hoverText(text: String): TellrawJson {
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

    fun hoverItem(id: String, tag: String = "{}"): TellrawJson {
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

    fun insertion(text: String): TellrawJson {
        componentsLatest.forEach { it.insertion = text }
        return this
    }

    fun openURL(url: String): TellrawJson {
        componentsLatest.forEach { it.clickEvent = ClickEvent(ClickEvent.Action.OPEN_URL, url) }
        return this
    }

    fun openFile(file: String): TellrawJson {
        componentsLatest.forEach { it.clickEvent = ClickEvent(ClickEvent.Action.OPEN_FILE, file) }
        return this
    }

    fun runCommand(command: String): TellrawJson {
        componentsLatest.forEach { it.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, command) }
        return this
    }

    fun changePage(page: Int): TellrawJson {
        componentsLatest.forEach { it.clickEvent = ClickEvent(ClickEvent.Action.CHANGE_PAGE, page.toString()) }
        return this
    }

    fun suggestCommand(command: String): TellrawJson {
        componentsLatest.forEach { it.clickEvent = ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command) }
        return this
    }

    fun copyToClipboard(text: String): TellrawJson {
        componentsLatest.forEach { it.clickEvent = ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, text) }
        return this
    }

    val componentsAll: List<BaseComponent>
        get() {
            val components = components.filter { it !is TextComponent || it.text.isNotEmpty() }.toMutableList()
            components.addAll(componentsLatest.filter { it !is TextComponent || it.text.isNotEmpty() })
            return components
        }

    private fun new() {
        components.addAll(componentsLatest)
        componentsLatest.clear()
    }

    override fun toString(): String {
        return toRawMessage()
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