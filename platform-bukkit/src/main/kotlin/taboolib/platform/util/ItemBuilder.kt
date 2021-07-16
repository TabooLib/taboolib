@file:Isolated

package taboolib.platform.util

import org.bukkit.Color
import org.bukkit.block.banner.Pattern
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.EntityType
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionData
import org.bukkit.potion.PotionEffect
import taboolib.common.Isolated
import taboolib.library.xseries.XMaterial

fun buildItem(material: XMaterial, builder: ItemBuilder.() -> Unit): ItemStack {
    return ItemBuilder(material).also(builder).build()
}

@Isolated
class ItemBuilder(val material: XMaterial) {

    var amount = 1
    var damage = 1.toShort()
    var name: String? = null
    val lore = ArrayList<String>()
    val flags = ArrayList<ItemFlag>()
    val enchants = HashMap<Enchantment, Int>()
    val patterns = ArrayList<Pattern>()
    var color: Color? = null
    val potions = ArrayList<PotionEffect>()
    var spawnType: EntityType? = null
    var potionData: PotionData? = null
    var skullOwner: String? = null
    var skullTexture: Pair<String, String>? = null
    var unbreakable = false
    var customModelData = -1

    fun shiny() {
        flags += ItemFlag.HIDE_ENCHANTS
        enchants[Enchantment.LURE] = 1
    }

    fun hideAll() {
        flags.addAll(ItemFlag.values())
    }

    fun build(): ItemStack {
        // TODO: 2021/7/17 No Implementations
        error(1)
    }
}