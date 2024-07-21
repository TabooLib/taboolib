package taboolib.module.chat

import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.*
import taboolib.common.reflect.Reflex.Companion.invokeMethod

/**
 * TabooLib
 * taboolib.module.chat.ComponentToJson
 *
 * @author 坏黑
 * @since 2023/2/26 22:25
 */
object ComponentToString {

    fun toLegacyString(vararg components: BaseComponent): String {
        val builder = StringBuilder()
        val newArray: Array<out BaseComponent> = components
        val size = components.size
        for (i in 0 until size) {
            builder.append(toLegacyString(newArray[i]))
        }
        return builder.toString()
    }

    private fun toLegacyString(component: BaseComponent): String {
        val builder = StringBuilder()
        toLegacyString1(component, builder)
        return builder.toString()
    }

    private fun toLegacyString1(component: BaseComponent, builder: StringBuilder): String {
        if (component is TranslatableComponent) {
            // 逻辑复杂，直接调用原始方法
            // 以防出现版本兼容问题
            component.invokeMethod("toLegacyText", builder)
        } else {
            addFormat(component, builder)
            when (component) {
                is TextComponent -> builder.append(component.text)
                is KeybindComponent -> builder.append(component.keybind)
                is ScoreComponent -> builder.append(component.value)
                is SelectorComponent -> builder.append(component.selector)
            }
        }
        component.extra?.forEach { toLegacyString1(it, builder) }
        return builder.toString()
    }

    private fun addFormat(component: BaseComponent, builder: StringBuilder) {
        if (component.colorRaw != null) {
            builder.append(component.color)
        }
        if (component.isBold) {
            builder.append(ChatColor.BOLD)
        }
        if (component.isItalic) {
            builder.append(ChatColor.ITALIC)
        }
        if (component.isUnderlined) {
            builder.append(ChatColor.UNDERLINE)
        }
        if (component.isStrikethrough) {
            builder.append(ChatColor.STRIKETHROUGH)
        }
        if (component.isObfuscated) {
            builder.append(ChatColor.MAGIC)
        }
    }
}