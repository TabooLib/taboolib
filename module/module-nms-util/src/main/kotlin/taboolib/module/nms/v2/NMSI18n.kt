package taboolib.module.nms.v2

import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffectType

fun ItemStack.getName(player: Player? = null): String {
    return if (itemMeta?.hasDisplayName() == true) itemMeta!!.displayName else getI18nName(player)
}

fun ItemStack.getI18nName(player: Player? = null): String {
    TODO("Not yet implemented")
}

fun Entity.getI18nName(player: Player? = null): String {
    TODO("Not yet implemented")
}

fun Enchantment.getI18nName(player: Player? = null): String {
    TODO("Not yet implemented")
}

fun PotionEffectType.getI18nName(player: Player? = null): String {
    TODO("Not yet implemented")
}