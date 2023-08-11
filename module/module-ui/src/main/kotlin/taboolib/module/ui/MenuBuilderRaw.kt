@file:Isolated

package taboolib.module.ui

import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import taboolib.common.Isolated
import taboolib.module.chat.Source

inline fun <reified T : Menu> buildMenu(title: Source, builder: T.() -> Unit): Inventory {
    return buildMenu(title.toRawMessage(), builder)
}

inline fun <reified T : Menu> Player.openMenu(title: Source, builder: T.() -> Unit) {
    openMenu(title.toRawMessage(), builder)
}