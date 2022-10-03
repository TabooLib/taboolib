package taboolib.expansion.geek

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.module.nms.getI18nName

/**
 * 作者: 老廖
 * 时间: 2022/9/4
 */
class ChineseMaterial {

    fun translate(material: Material, player: Player? = null): String {
        return ItemStack(material).getI18nName(player)
    }
}