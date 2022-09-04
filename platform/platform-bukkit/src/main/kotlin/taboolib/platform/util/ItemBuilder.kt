@file:Isolated

package taboolib.platform.util

import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import org.bukkit.ChatColor
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
import org.tabooproject.reflex.Reflex.Companion.getProperty
import org.tabooproject.reflex.Reflex.Companion.invokeMethod
import org.tabooproject.reflex.Reflex.Companion.setProperty
import taboolib.library.xseries.XMaterial
import taboolib.library.xseries.XSkull
import taboolib.module.chat.colored
import java.util.*

val ItemStack?.isAir get() = isAir()

fun buildItem(itemStack: ItemStack, builder: ItemBuilder.() -> Unit = {}): ItemStack {
    if (itemStack.isAir) {
        error("air")
    }
    return ItemBuilder(itemStack).also(builder).build()
}

val XMaterial.isAir get() = this == XMaterial.AIR || this == XMaterial.CAVE_AIR || this == XMaterial.VOID_AIR

fun buildItem(material: XMaterial, builder: ItemBuilder.() -> Unit = {}): ItemStack {
    if (material.isAir) {
        error("air")
    }
    return ItemBuilder(material).also(builder).build()
}

fun buildItem(material: Material, builder: ItemBuilder.() -> Unit = {}): ItemStack {
    // 在低版本, 并没有 Material.isAir() 方法.
    if (material == Material.AIR) {
        error("air")
    }
    return ItemBuilder(material).also(builder).build()
}

@Isolated
open class ItemBuilder {

    class SkullTexture(val textures: String, val uuid: UUID? = UUID.randomUUID())

    var material: Material
    var amount = 1
    var damage = 0
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
    var isUnbreakable = false
    var customModelData = -1

    // 尝试修复自定义 nbt 失效
    var originMeta: ItemMeta? = null

    var finishing: (ItemStack) -> Unit = {}

    constructor(material: Material) {
        this.material = material
    }

    constructor(material: XMaterial): this(material.parseMaterial() ?: Material.STONE) {
        if (!XMaterial.supports(13)) {
            this.damage = material.data.toInt()
        }
    }

    constructor(item: ItemStack) {
        material = item.type
        amount = item.amount
        damage = item.durability.toInt()
        val itemMeta = item.itemMeta ?: return
        originMeta = itemMeta
        name = itemMeta.displayName
        lore += itemMeta.lore ?: emptyList()
        flags += itemMeta.itemFlags
        enchants += if (itemMeta is EnchantmentStorageMeta) {
            itemMeta.storedEnchants
        } else {
            itemMeta.enchants
        }
        when (itemMeta) {
            is LeatherArmorMeta -> {
                color = itemMeta.color
            }
            is PotionMeta -> {
                color = itemMeta.color
                potions += itemMeta.customEffects
                potionData = itemMeta.basePotionData
            }
            is SkullMeta -> {
                if (itemMeta.owner != null) {
                    skullOwner = itemMeta.owner
                }
                skullTexture = XSkull.getSkinValue(itemMeta)
            }
        }
        try {
            customModelData = itemMeta.getProperty<Int>("customModelData") ?: -1
        } catch (_: NoSuchFieldException) {
        }
        try {
            isUnbreakable = itemMeta.isUnbreakable
        } catch (ex: NoSuchMethodError) {
            try {
                isUnbreakable = itemMeta.invokeMethod<Any>("spigot")!!.invokeMethod<Boolean>("isUnbreakable") ?: false
            } catch (_: NoSuchMethodException) {
            }
        }
        try {
            if (itemMeta is SpawnEggMeta && itemMeta.spawnedType != null) {
                spawnType = itemMeta.spawnedType
            }
        } catch (_: NoClassDefFoundError) {
        } catch (_: UnsupportedOperationException) {

        }
        try {
            if (itemMeta is BannerMeta && itemMeta.patterns.isNotEmpty()) {
                patterns += itemMeta.patterns
            }
        } catch (_: NoClassDefFoundError) {
        }
    }

    fun setMaterial(material: XMaterial) {
        this.material = material.parseMaterial() ?: Material.STONE
    }

    fun shiny() {
        flags += ItemFlag.HIDE_ENCHANTS
        enchants[Enchantment.LURE] = 1
    }

    fun hideAll() {
        flags.addAll(ItemFlag.values())
    }

    fun colored() {
        if (name != null) {
            name = try {
                name!!.colored()
            } catch (ex: NoClassDefFoundError) {
                ChatColor.translateAlternateColorCodes('&', name!!)
            }
        }
        if (lore.isNotEmpty()) {
            val newLore = try {
                lore.colored()
            } catch (ex: NoClassDefFoundError) {
                lore.map { ChatColor.translateAlternateColorCodes('&', it) }
            }
            lore.clear()
            lore += newLore
        }
    }

    open fun build(): ItemStack {
        val itemStack = ItemStack(material)
        itemStack.amount = amount
        if (damage != 0) {
            itemStack.durability = damage.toShort()
        }
        val itemMeta = originMeta ?: itemStack.itemMeta ?: return itemStack
        itemMeta.setDisplayName(name)
        itemMeta.lore = lore
        itemMeta.addItemFlags(*flags.toTypedArray())
        if (itemMeta is EnchantmentStorageMeta) {
            enchants.forEach { (e, lvl) -> itemMeta.addStoredEnchant(e, lvl, true) }
        } else {
            enchants.forEach { (e, lvl) -> itemMeta.addEnchant(e, lvl, true) }
        }
        when (itemMeta) {
            is LeatherArmorMeta -> {
                itemMeta.setColor(color)
            }
            is PotionMeta -> {
                itemMeta.color = color
                potions.forEach { itemMeta.addCustomEffect(it, true) }
                if (potionData != null) {
                    itemMeta.basePotionData = potionData!!
                }
            }
            is SkullMeta -> {
                if (skullOwner != null) {
                    itemMeta.owner = skullOwner
                }
                if (skullTexture != null) {
                    itemMeta.setProperty("profile", GameProfile(skullTexture!!.uuid, null).also {
                        it.properties.put("textures", Property("textures", skullTexture!!.textures))
                    })
                }
            }
        }
        try {
            itemMeta.isUnbreakable = isUnbreakable
        } catch (ex: NoSuchMethodError) {
            try {
                itemMeta.invokeMethod<Any>("spigot")!!.invokeMethod<Any>("setUnbreakable", isUnbreakable)
            } catch (ignored: NoSuchMethodException) {
            }
        }
        try {
            if (spawnType != null && itemMeta is SpawnEggMeta) {
                itemMeta.spawnedType = spawnType
            }
        } catch (ignored: NoClassDefFoundError) {
        }
        try {
            if (patterns.isNotEmpty() && itemMeta is BannerMeta) {
                patterns.forEach { itemMeta.addPattern(it) }
            }
        } catch (ignored: NoClassDefFoundError) {
        }
        try {
            if (customModelData != -1) {
                itemMeta.invokeMethod<Void>("setCustomModelData", customModelData)
            }
        } catch (ignored: NoSuchMethodException) {
        }
        itemStack.itemMeta = itemMeta
        finishing(itemStack)
        return itemStack
    }
}