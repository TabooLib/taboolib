package taboolib.module.ui.virtual

import org.bukkit.event.inventory.ClickType

/**
 * TabooLib
 * taboolib.module.ui.virtual.VirtualClickType
 *
 * @author 坏黑
 * @since 2023/3/20 18:34
 */
enum class VirtualClickType {

    LEFT, SHIFT_LEFT, RIGHT, SHIFT_RIGHT, WINDOW_BORDER_LEFT, WINDOW_BORDER_RIGHT, MIDDLE, NUMBER_KEY, DOUBLE_CLICK, DROP, CONTROL_DROP, CREATIVE, SWAP_OFFHAND, UNKNOWN;

    fun toBukkit(): ClickType {
        return ClickType.values()[ordinal]
    }
}