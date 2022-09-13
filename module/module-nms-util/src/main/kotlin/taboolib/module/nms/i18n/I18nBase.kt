package taboolib.module.nms.i18n

import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffectType

/**
 * 原版语言文件实现接口
 *
 * @author sky
 * @since 2020-04-04 19:42
 */
abstract class I18nBase {

    abstract fun init()

    /**
     * 获取实体对应译名
     * 如果传入玩家则通过该玩家的客户端语言获取对应译名
     *
     * @param player 玩家
     * @param entity 实体
     * @return String
     */
    abstract fun getName(player: Player?, entity: Entity): String

    /**
     * 获取物品对应译名
     * 如果传入玩家则通过该玩家的客户端语言获取对应译名
     *
     * @param player    玩家
     * @param itemStack 物品
     * @return String
     */
    abstract fun getName(player: Player?, itemStack: ItemStack): String

    /**
     * 获取附魔对应译名
     * 如果传入玩家则通过该玩家的客户端语言获取对应译名
     *
     * @param player      玩家
     * @param enchantment 附魔
     * @return String
     */
    abstract fun getName(player: Player?, enchantment: Enchantment): String

    /**
     * 获取药水效果对应译名
     * 如果传入玩家则通过该玩家的客户端语言获取对应译名
     *
     * @param player           玩家
     * @param potionEffectType 药水效果
     * @return String
     */
    abstract fun getName(player: Player?, potionEffectType: PotionEffectType): String

    /**
     * 获取实体对应中文译名
     *
     * @param entity 实体
     * @return String
     */
    fun getName(entity: Entity): String {
        return getName(null, entity)
    }

    /**
     * 获取物品对应译名
     *
     * @param itemStack 物品
     * @return String
     */
    fun getName(itemStack: ItemStack): String {
        return getName(null, itemStack)
    }

    /**
     * 获取附魔对应中文译名
     *
     * @param enchantment 实体
     * @return String
     */
    fun getName(enchantment: Enchantment): String {
        return getName(null, enchantment)
    }

    /**
     * 获取药水效果对应译名
     *
     * @param potionEffectType 药水效果
     * @return String
     */
    fun getName(potionEffectType: PotionEffectType): String {
        return getName(null, potionEffectType)
    }
}