@file:Suppress("DEPRECATION")

package taboolib.platform.util

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
import org.tabooproject.reflex.Reflex.Companion.getProperty
import org.tabooproject.reflex.Reflex.Companion.invokeMethod
import taboolib.library.xseries.XMaterial
import taboolib.library.xseries.XSkull
import taboolib.module.chat.colored

/**
 * 判定材质是否为空气
 */
val XMaterial.isAir get() = this == XMaterial.AIR || this == XMaterial.CAVE_AIR || this == XMaterial.VOID_AIR

/**
 * 判定物品是否为空气
 */
val ItemStack?.isAir get() = isAir()

/**
 * 通过现有物品构建新的物品
 *
 * @param itemStack 原始物品
 * @param builder 构建器
 * @return 新的物品
 * @throws IllegalArgumentException 如果物品为空气
 */
fun buildItem(itemStack: ItemStack, builder: ItemBuilder.() -> Unit = {}): ItemStack {
    if (itemStack.isAir) {
        error("air")
    }
    return ItemBuilder(itemStack).also(builder).build()
}

/**
 * 通过 [XMaterial] 材质构建新的物品
 *
 * @param material 材质
 * @param builder 构建器
 * @return 新的物品
 * @throws IllegalArgumentException 如果材质为空气
 */
fun buildItem(material: XMaterial, builder: ItemBuilder.() -> Unit = {}): ItemStack {
    if (material.isAir) {
        error("air")
    }
    return ItemBuilder(material).also(builder).build()
}

/**
 * 通过 [Material] 材质构建新的物品
 *
 * @param material 材质
 * @param builder 构建器
 * @return 新的物品
 * @throws IllegalArgumentException 如果材质为空气
 */
fun buildItem(material: Material, builder: ItemBuilder.() -> Unit = {}): ItemStack {
    // 在低版本, 并没有 Material.isAir() 方法
    if (material == Material.AIR) {
        error("air")
    }
    return ItemBuilder(material).also(builder).build()
}

open class ItemBuilder {

    /**
     * 物品材质
     */
    var material: Material

    /**
     * 数量
     */
    var amount = 1

    /**
     * 附加值（损伤值）
     */
    var damage = 0

    /**
     * 展示名称
     */
    var name: String? = null

    /**
     * 描述
     */
    val lore = ArrayList<String>()

    /**
     * 标签
     */
    val flags = ArrayList<ItemFlag>()

    /**
     * 附魔
     */
    val enchants = HashMap<Enchantment, Int>()

    /**
     * 旗帜花纹
     */
    val patterns = ArrayList<Pattern>()

    /**
     * 颜色
     */
    var color: Color? = null

    /**
     * 药水效果
     */
    val potions = ArrayList<PotionEffect>()

    /**
     * 基础药水效果
     */
    var potionData: PotionData? = null

    /**
     * 生物类型
     */
    var spawnType: EntityType? = null

    /**
     * 头颅信息
     */
    var skullOwner: String? = null

    /**
     * 头颅材质信息
     */
    var skullTexture: XSkull.SkullTexture? = null

    /**
     * 无法破坏
     */
    var isUnbreakable = false

    /**
     * CustomModelData
     */
    var customModelData = -1

    /**
     * 原始数据
     * 尝试修复自定义 nbt 失效的问题
     */
    var originMeta: ItemMeta? = null

    /**
     * 当构建完成时，做出最后修改
     */
    var finishing: (ItemStack) -> Unit = {}

    /**
     * 设置材质
     */
    fun setMaterial(material: XMaterial) {
        this.material = material.parseMaterial() ?: Material.STONE
    }

    /**
     * 使其发光
     */
    fun shiny() {
        flags += ItemFlag.HIDE_ENCHANTS
        enchants[Enchantment.LURE] = 1
    }

    /**
     * 隐藏所有附加信息（赋予所有 ItemFlag）
     */
    fun hideAll() {
        flags.addAll(ItemFlag.values())
    }

    /**
     * 上色
     */
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

    /**
     * 构建物品
     */
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
                potions.forEach { itemMeta.addCustomEffect(it, true) }
                if (color != null) {
                    itemMeta.color = color
                }
                if (potionData != null) {
                    itemMeta.basePotionData = potionData!!
                }
            }

            is SkullMeta -> {
                if (skullOwner != null) {
                    itemMeta.owner = skullOwner
                }
                if (skullTexture != null) {
                    XSkull.applySkin(itemMeta, skullTexture!!.texture)
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

    constructor(material: Material) {
        this.material = material
    }

    constructor(material: XMaterial) : this(material.parseMaterial() ?: Material.STONE) {
        if (!XMaterial.supports(13)) {
            this.damage = material.data.toInt()
        }
    }

    /**
     * 通过现有物品构建新的物品
     * 读取基本信息
     */
    constructor(item: ItemStack) {
        material = item.type
        amount = item.amount
        damage = item.durability.toInt()
        // 如果物品没有 ItemMeta 则不进行后续操作
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
                XSkull.getSkinValue(itemMeta)?.let { skullTexture = it }
            }
        }
        try {
            customModelData = itemMeta.getProperty<Int>("customModelData") ?: -1
        } catch (ignored: NoSuchFieldException) {
        }
        try {
            isUnbreakable = itemMeta.isUnbreakable
        } catch (ex: NoSuchMethodError) {
            try {
                isUnbreakable = itemMeta.invokeMethod<Any>("spigot")!!.invokeMethod<Boolean>("isUnbreakable") ?: false
            } catch (ignored: NoSuchMethodException) {
            }
        }
        try {
            if (itemMeta is SpawnEggMeta && itemMeta.spawnedType != null) {
                spawnType = itemMeta.spawnedType
            }
        } catch (ignored: NoClassDefFoundError) {
        } catch (ignored: UnsupportedOperationException) {

        }
        try {
            if (itemMeta is BannerMeta && itemMeta.patterns.isNotEmpty()) {
                patterns += itemMeta.patterns
            }
        } catch (ignored: NoClassDefFoundError) {
        }
    }

}
