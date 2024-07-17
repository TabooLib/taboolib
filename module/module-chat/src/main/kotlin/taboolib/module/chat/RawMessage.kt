package taboolib.module.chat

import taboolib.common.platform.ProxyCommandSender

@Deprecated("use Components instead.")
typealias TellrawJson = RawMessage

/**
 * @author sky
 * @since 2018-05-26 14:42json
 */
@Deprecated("use Components instead.")
@Suppress("ALL")
class RawMessage(val component: ComponentText = Components.empty()) {

    fun sendTo(target: ProxyCommandSender, builder: RawMessage.() -> Unit = {}) {
        builder(this)
        component.sendTo(target)
    }

    fun broadcast(builder: RawMessage.() -> Unit = {}) {
        builder(this)
        component.broadcast()
    }

    fun toRawMessage(): String {
        return component.toRawMessage()
    }

    fun toLegacyText(): String {
        return component.toLegacyText()
    }

    fun toPlainText(): String {
        return component.toPlainText()
    }

    fun newLine(): RawMessage {
        return append("\n")
    }

    fun append(text: String): RawMessage {
        component.append(text)
        return this
    }

    fun append(other: RawMessage): RawMessage {
        component.append(other.component)
        return this
    }

    fun appendTranslatable(node: String, vararg obj: Any): RawMessage {
        component.appendTranslation(node, *obj)
        return this
    }

    fun appendKeybind(keybind: String): RawMessage {
        component.appendKeybind(keybind)
        return this
    }

    fun appendScore(name: String, objective: String): RawMessage {
        component.appendScore(name, objective)
        return this
    }

    fun appendSelector(selector: String): RawMessage {
        component.appendSelector(selector)
        return this
    }

    fun hoverText(text: String): RawMessage {
        component.hoverText(text)
        return this
    }

    fun hoverText(text: RawMessage): RawMessage {
        component.hoverText(text.component)
        return this
    }

    fun hoverItem(id: String, tag: String = "{}"): RawMessage {
        component.hoverItem(id, tag)
        return this
    }

    fun insertion(text: String): RawMessage {
        component.clickInsertText(text)
        return this
    }

    fun openURL(url: String): RawMessage {
        component.clickOpenURL(url)
        return this
    }

    fun openFile(file: String): RawMessage {
        component.clickOpenFile(file)
        return this
    }

    fun runCommand(command: String): RawMessage {
        component.clickRunCommand(command)
        return this
    }

    fun changePage(page: Int): RawMessage {
        component.clickChangePage(page)
        return this
    }

    fun suggestCommand(command: String): RawMessage {
        component.clickSuggestCommand(command)
        return this
    }

    fun copyToClipboard(text: String): RawMessage {
        component.clickCopyToClipboard(text)
        return this
    }

    fun font(font: String): RawMessage {
        component.font(font)
        return this
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