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

    /**
     * 发送信息
     *
     * @param target 目标
     * @param builder 信息构建器
     */
    fun sendTo(target: ProxyCommandSender, builder: TellrawJson.() -> Unit = {}) {
        builder(this)
        if (target is ProxyPlayer) {
            target.sendRawMessage(toRawMessage())
        } else {
            target.sendMessage(toLegacyText())
        }
    }

    /**
     * 向所有玩家播报信息
     *
     * @param builder 信息构建器
     */
    fun broadcast(builder: TellrawJson.() -> Unit = {}) {
        builder(this)
        onlinePlayers().forEach { p -> sendTo(p) }
    }

    /**
     * 转换为原始信息
     */
    fun toRawMessage(): String {
        return ComponentSerializer.toString(*componentsAll.toTypedArray())
    }

    fun toLegacyText(): String {
        return TextComponent.toLegacyText(*componentsAll.toTypedArray())
    }

    fun toPlainText(): String {
        return TextComponent.toPlainText(*componentsAll.toTypedArray())
    }

    /**
     * 换行
     *
     * @return [TellrawJson]
     */
    fun newLine(): TellrawJson {
        return append("\n")
    }

    /**
     * 添加文本块
     *
     * @param text 文本
     * @return [TellrawJson]
     */
    fun append(text: String): TellrawJson {
        new()
        componentsLatest.addAll(TextComponent.fromLegacyText(text))
        return this
    }

    /**
     * 添加翻译文本块
     *
     * @param node 语言文件节点
     * @param obj 参数
     * @return [TellrawJson]
     */
    fun appendTranslatable(node: String, vararg obj: Any): TellrawJson {
        new()
        componentsLatest.add(TranslatableComponent(node, obj))
        return this
    }

    /**
     * 添加按键文本块
     *
     * @param keybind 按键
     * @return [TellrawJson]
     */
    @Suppress("SpellCheckingInspection")
    fun appendKeybind(keybind: String): TellrawJson {
        new()
        componentsLatest.add(KeybindComponent(keybind))
        return this
    }

    /**
     * 添加分数文本块
     *
     * @param name 分数名
     * @param objective 分数目标
     * @return [TellrawJson]
     */
    fun appendScore(name: String, objective: String): TellrawJson {
        new()
        componentsLatest.add(ScoreComponent(name, objective))
        return this
    }

    /**
     * 添加选择文本块
     *
     * @param selector 选择器
     * @return [TellrawJson]
     */
    fun appendSelector(selector: String): TellrawJson {
        new()
        componentsLatest.add(SelectorComponent(selector))
        return this
    }

    /**
     * 追加另一个 [TellrawJson]
     *
     * @return [TellrawJson]
     */
    fun append(json: TellrawJson): TellrawJson {
        new()
        componentsLatest.addAll(json.componentsAll)
        return this
    }

    /**
     * 为上一个文本块添加悬浮文本
     *
     * @param text 悬浮文本
     * @return [TellrawJson]
     */
    @Suppress("DEPRECATION")
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

    /**
     * 为上一个文本块添加悬浮物品
     *
     * @param id 物品序号
     * @param tag 物品 NBT
     * @return [TellrawJson]
     */
    @Suppress("DEPRECATION")
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

    /**
     * 为上一个文本块添加 OPEN_URL 行为
     *
     * @param url 网址
     * @return [TellrawJson]
     */
    fun openURL(url: String): TellrawJson {
        componentsLatest.forEach { it.clickEvent = ClickEvent(ClickEvent.Action.OPEN_URL, url) }
        return this
    }

    /**
     * 为上一个文本块添加 OPEN_FILE 行为
     *
     * @param file 文件路径
     * @return [TellrawJson]
     */
    fun openFile(file: String): TellrawJson {
        componentsLatest.forEach { it.clickEvent = ClickEvent(ClickEvent.Action.OPEN_FILE, file) }
        return this
    }

    /**
     * 为上一个文本块添加 RUN_COMMAND 行为
     *
     * @param command 命令
     * @return [TellrawJson]
     */
    fun runCommand(command: String): TellrawJson {
        componentsLatest.forEach { it.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, command) }
        return this
    }

    /**
     * 为上一个文本块添加 CHANGE_PAGE 行为
     *
     * @param page 页码
     * @return [TellrawJson]
     */
    fun changePage(page: Int): TellrawJson {
        componentsLatest.forEach { it.clickEvent = ClickEvent(ClickEvent.Action.CHANGE_PAGE, page.toString()) }
        return this
    }

    /**
     * 为上一个文本块添加 SUGGEST_COMMAND 行为
     *
     * @param command 命令
     * @return [TellrawJson]
     */
    fun suggestCommand(command: String): TellrawJson {
        componentsLatest.forEach { it.clickEvent = ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command) }
        return this
    }

    /**
     * 为上一个文本块添加 COPY_TO_CLIPBOARD 行为
     *
     * @param text 文本
     * @return [TellrawJson]
     */
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

        @Suppress("SpellCheckingInspection")
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