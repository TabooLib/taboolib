@file:Isolated

package taboolib.platform.util

import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.block.banner.Pattern
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.EntityType
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.*
import org.bukkit.potion.PotionData
import org.bukkit.potion.PotionEffect
import taboolib.common.Isolated
import taboolib.common.platform.warning
import taboolib.common.reflect.Reflex.Companion.reflex
import taboolib.common.reflect.Reflex.Companion.reflexInvoke
import taboolib.library.xseries.XMaterial
import java.util.*

fun buildItem(material: XMaterial, builder: ItemBuilder.() -> Unit): ItemStack {
    if (material == XMaterial.AIR || material == XMaterial.CAVE_AIR || material == XMaterial.VOID_AIR) {
        error("is air")
    }
    return ItemBuilder(material).also(builder).build()
}

@Isolated
class ItemBuilder(val material: XMaterial) {

    class SkullTexture(val textures: String, val uuid: UUID? = null)

    var amount = 1
    var damage = 1.toShort()
    var name: String? = null
    val lore = ArrayList<String>()
    val flags = ArrayList<ItemFlag>()
    val enchants = HashMap<Enchantment, Int>()
    val patterns = ArrayList<Pattern>()
    var color: Color? = null
    val potions = ArrayList<PotionEffect>()
    var potionData: PotionData? = null
    var spawnType: EntityType? = null
    var skullOwner: String? = null
    var skullTexture: SkullTexture? = null
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
        val itemStack = material.parseItem() ?: ItemStack(Material.STONE)
        val itemMeta = itemStack.itemMeta!!
        itemMeta.displayName = name
        itemMeta.lore = lore
        itemMeta.addItemFlags(*flags.toTypedArray())
        if (itemMeta is EnchantmentStorageMeta) {
            enchants.forEach { (e, lvl) -> itemMeta.addStoredEnchant(e, lvl, true) }
        } else {
            enchants.forEach { (e, lvl) -> itemMeta.addEnchant(e, lvl, true) }
        }
        when (itemMeta) {
            is LeatherArmorMeta -> {
                itemMeta.color = color
            }
            is PotionMeta -> {
                itemMeta.color = color
                potions.forEach { itemMeta.addCustomEffect(it, true) }
                if (potionData != null) {
                    itemMeta.basePotionData = potionData!!
                }
            }
            is SpawnEggMeta -> {
                itemMeta.spawnedType = spawnType
            }
            is SkullMeta -> {
                if (skullOwner != null) {
                    itemMeta.owner = skullOwner
                }
                if (skullTexture != null) {
                    itemMeta.reflex("profile", GameProfile(skullTexture!!.uuid, null).also {
                        it.properties.put("textures", Property("textures", skullTexture!!.textures))
                    })
                }
            }
        }
        try {
            itemMeta.isUnbreakable = unbreakable
        } catch (ex: NoSuchMethodError) {
            try {
                itemMeta.spigot().isUnbreakable = unbreakable
            } catch (ex: NoSuchMethodError) {
                warning("Unbreakable not supported yet.")
            }
        }
        try {
            if (patterns.isNotEmpty() && itemMeta is BannerMeta) {
                patterns.forEach { itemMeta.addPattern(it) }
            }
        } catch (ex: NoClassDefFoundError) {
            warning("BannerMeta not supported yet.")
        }
        try {
            if (customModelData != -1) {
                itemMeta.reflexInvoke<Void>("setCustomModelData", customModelData)
            }
        } catch (ex: NoSuchMethodException) {
            warning("CustomModelData not supported yet.")
        }
        itemStack.itemMeta = itemMeta
        return itemStack
    }
}