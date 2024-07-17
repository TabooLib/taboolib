package taboolib.module.nms

import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffectType

/**
 * 获取物品的名称（若存在 displayName 则返回 displayName，反之获取译名）
 */
fun ItemStack.getName(player: Player? = null): String {
    return if (itemMeta?.hasDisplayName() == true) itemMeta!!.displayName else getI18nName(player)
}

/**
 * 获取物品的译名
 */
fun ItemStack.getI18nName(player: Player? = null): String {
    val localeFile = player?.getLocaleFile() ?: LocaleI18n.getDefaultLocaleFile() ?: return "NO_LOCALE"
    return localeFile[getLocaleKey()] ?: getLocaleKey().path
}

/**
 * 获取实体的译名
 */
fun Entity.getI18nName(player: Player? = null): String {
    val localeFile = player?.getLocaleFile() ?: LocaleI18n.getDefaultLocaleFile() ?: return "NO_LOCALE"
    return localeFile[getLocaleKey()] ?: getLocaleKey().path
}

/**
 * 获取附魔的译名
 */
fun Enchantment.getI18nName(player: Player? = null): String {
    val localeFile = player?.getLocaleFile() ?: LocaleI18n.getDefaultLocaleFile() ?: return "NO_LOCALE"
    return localeFile[getLocaleKey()] ?: getLocaleKey().path
}

/**
 * 获取药水效果的译名
 */
fun PotionEffectType.getI18nName(player: Player? = null): String {
    val localeFile = player?.getLocaleFile() ?: LocaleI18n.getDefaultLocaleFile() ?: return "NO_LOCALE"
    return localeFile[getLocaleKey()] ?: getLocaleKey().path
}