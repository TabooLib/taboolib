package taboolib.module.nms

import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffectType
import taboolib.common.util.unsafeLazy
import taboolib.module.nms.i18n.I18n

val nmsGeneric by unsafeLazy { nmsProxy<NMSGeneric>() }

/**
 * 获得物品的名称，如果没有则返回译名
 */
fun ItemStack.getName(player: Player? = null): String {
    return if (itemMeta?.hasDisplayName() == true) itemMeta!!.displayName else getI18nName(player)
}

/**
 * 获取物品的译名
 */
fun ItemStack.getI18nName(player: Player? = null): String {
    return I18n.instance.getName(player, this)
}

/**
 * 获取物品的内部名称
 */
fun ItemStack.getInternalKey(): String {
    return nmsGeneric.getKey(this)
}

/**
 * 获取物品的内部名称
 */
fun ItemStack.getInternalName(): String {
    return nmsGeneric.getName(this)
}

/**
 * 获取实体的译名
 */
fun Entity.getI18nName(player: Player? = null): String {
    return I18n.instance.getName(player, this)
}

/**
 * 获取实体的内部名称
 */
fun Entity.getInternalName(): String {
    return nmsGeneric.getName(this)
}

/**
 * 获取附魔的译名
 */
fun Enchantment.getI18nName(player: Player? = null): String {
    return I18n.instance.getName(player, this)
}

/**
 * 获取附魔的内部名称
 */
fun Enchantment.getInternalName(): String {
    return nmsGeneric.getEnchantmentKey(this)
}

/**
 * 获取药水效果的译名
 */
fun PotionEffectType.getI18nName(player: Player? = null): String {
    return I18n.instance.getName(player, this)
}

/**
 * 获取药水效果的内部名称
 */
fun PotionEffectType.getInternalName(): String {
    return nmsGeneric.getPotionEffectTypeKey(this)
}