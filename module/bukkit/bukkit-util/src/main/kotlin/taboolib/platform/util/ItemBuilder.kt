@file:Suppress("DEPRECATION", "removal")

package taboolib.platform.util

import org.bukkit.ChatColor
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
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
import taboolib.common.util.random
import taboolib.library.xseries.XMaterial
import taboolib.module.chat.colored
import java.util.*

/**
 * 通过现有物品构建新的物品
 *
 * @param itemStack 原始物品
 * @param builder 构建器
 * @return 新的物品
 * @throws IllegalArgumentException 如果物品为空气
 */
fun buildItem(itemStack: ItemStack, builder: ItemBuilder.() -> Unit = {}): ItemStack {
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
     * 依赖 "bukkit-xseries-skull" 模块
     */
    var skullTexture: SkullTexture? = null

    /**
     * 无法破坏
     */
    var isUnbreakable = false

    /**
     * CustomModelData
     */
    var customModelData = -1

    /**
     * 唯一化
     */
    var unique = false

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
     * 唯一化
     * 写入一个唯一的属性以避免被合并，通常用于页面元件
     * 在低版本不可用
     */
    fun unique() {
        unique = true
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
        var itemStack = ItemStack(material)
        // 数量
        itemStack.amount = amount
        // 耐久（附加值）
        if (damage != 0) itemStack.durability = damage.toShort()

        // 开始构建 ItemMeta
        val itemMeta = originMeta ?: itemStack.itemMeta ?: return itemStack
        // 展示名称
        itemMeta.setDisplayName(name)
        // 描述
        itemMeta.lore = lore
        // 标签
        itemMeta.addItemFlags(*flags.toTypedArray())

        // 附魔
        // 对附魔书进行特殊处理
        if (itemMeta is EnchantmentStorageMeta) {
            enchants.forEach { (e, lvl) -> itemMeta.addStoredEnchant(e, lvl, true) }
        } else {
            enchants.forEach { (e, lvl) -> itemMeta.addEnchant(e, lvl, true) }
        }

        // 处理特殊类型
        when (itemMeta) {
            // 颜色
            is LeatherArmorMeta -> itemMeta.setColor(color)
            // 药水
            is PotionMeta -> {
                // 自定义药水效果
                potions.forEach { itemMeta.addCustomEffect(it, true) }
                // 药水颜色
                if (color != null) itemMeta.color = color
                // 基础药水类型（过时）
                if (potionData != null) itemMeta.basePotionData = potionData!!
            }
            // 头
            is SkullMeta -> {
                // 玩家头颅
                if (skullOwner != null) itemMeta.owner = skullOwner
            }
        }

        // 无法破坏
        try {
            itemMeta.isUnbreakable = isUnbreakable
        } catch (ex: NoSuchMethodError) {
            try {
                itemMeta.invokeMethod<Any>("spigot")!!.invokeMethod<Any>("setUnbreakable", isUnbreakable)
            } catch (_: NoSuchMethodException) {
            }
        }
        // 蛋
        try {
            if (spawnType != null && itemMeta is SpawnEggMeta) {
                itemMeta.spawnedType = spawnType
            }
        } catch (_: NoClassDefFoundError) {
        }
        // 旗帜
        try {
            if (patterns.isNotEmpty() && itemMeta is BannerMeta) {
                patterns.forEach { itemMeta.addPattern(it) }
            }
        } catch (_: NoClassDefFoundError) {
        }
        // CustomModelData
        try {
            if (customModelData != -1) {
                itemMeta.invokeMethod<Void>("setCustomModelData", customModelData)
            }
        } catch (_: NoSuchMethodException) {
        }
        // 唯一化
        try {
            if (unique) {
                val modifier = AttributeModifier(UUID.randomUUID(), "unique", random(0.0, 1.0), AttributeModifier.Operation.ADD_NUMBER)
                itemMeta.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, modifier)
                itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
            }
        } catch (_: NoSuchMethodError) {
        }

        // 返回
        itemStack.itemMeta = itemMeta
        // 基于 BukkitSkull 工具设置头
        if (itemMeta is SkullMeta && skullTexture != null) {
            itemStack = BukkitSkull.applySkull(itemStack, skullTexture!!.texture)
        }
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

        // 基本信息
        name = itemMeta.displayName
        lore += itemMeta.lore ?: emptyList()
        flags += itemMeta.itemFlags

        // 附魔
        // 对附魔书进行特殊处理
        enchants.putAll(if (itemMeta is EnchantmentStorageMeta) itemMeta.storedEnchants else itemMeta.enchants)

        // 特殊类型
        when (itemMeta) {
            // 颜色
            is LeatherArmorMeta -> color = itemMeta.color
            // 药水
            is PotionMeta -> {
                color = itemMeta.color
                potions += itemMeta.customEffects
                potionData = itemMeta.basePotionData
            }
            // 头
            is SkullMeta -> {
                // 玩家
                skullOwner = itemMeta.owner
                // 皮肤信息
                itemMeta.getSkullValue()?.let { skullTexture = SkullTexture(it) }
            }
        }
        // 无法破坏
        try {
            isUnbreakable = itemMeta.isUnbreakable
        } catch (ex: NoSuchMethodError) {
            try {
                isUnbreakable = itemMeta.invokeMethod<Any>("spigot")!!.invokeMethod<Boolean>("isUnbreakable") ?: false
            } catch (ignored: NoSuchMethodException) {
            }
        }
        // 刷怪蛋
        try {
            if (itemMeta is SpawnEggMeta && itemMeta.spawnedType != null) {
                spawnType = itemMeta.spawnedType
            }
        } catch (ignored: NoClassDefFoundError) {
        } catch (ignored: UnsupportedOperationException) {
        }
        // 旗帜
        try {
            if (itemMeta is BannerMeta && itemMeta.patterns.isNotEmpty()) {
                patterns += itemMeta.patterns
            }
        } catch (ignored: NoClassDefFoundError) {
        }
        // CustomModelData
        try {
            customModelData = itemMeta.getProperty<Int>("customModelData") ?: -1
        } catch (ignored: NoSuchFieldException) {
        }
    }
}

data class SkullTexture(val texture: String, val uuid: UUID = UUID(0, 0)) {

    /**
     * 如果 UUID 不是 0 0
     */
    fun isUUIDSet(): Boolean {
        return uuid != UUID(0, 0)
    }
}
