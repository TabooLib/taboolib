@file:Isolated

package taboolib.platform.util

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.common.Isolated
import taboolib.module.nms.getI18nName

/**
 * 获得物品的名称，如果没有则返回译名
 */
fun ItemStack.getName(player: Player? = null): String {
    return if (hasName()) itemMeta!!.displayName else getI18nName(player)
}