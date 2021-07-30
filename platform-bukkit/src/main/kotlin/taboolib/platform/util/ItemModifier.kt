@file:Isolated

package taboolib.platform.util

import com.google.common.collect.ImmutableMap
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import taboolib.common.Isolated

fun Material?.isAir(): Boolean {
    return this == null || this == Material.AIR || this.name.endsWith("_AIR")
}

fun Material?.isNotAir(): Boolean {
    return !isAir()
}

fun ItemStack?.isAir(): Boolean {
    return this == null || type == Material.AIR || type.name.endsWith("_AIR")
}

fun ItemStack?.isNotAir(): Boolean {
    return !isAir()
}

/**
 * 编辑物品元数据
 */
fun ItemStack.modifyMeta(func: ItemMeta.() -> Unit): ItemStack {
    if (isAir()) {
        error("air")
    }
    return also { itemMeta = itemMeta!!.also(func) }
}

/**
 * 编辑物品描述
 */
fun ItemMeta.modifyLore(func: MutableList<String>.() -> Unit): ItemMeta {
    return also { lore = (lore ?: ArrayList<String>()).also(func) }
}

/**
 * 编辑物品描述
 */
fun ItemStack.modifyLore(func: MutableList<String>.() -> Unit): ItemStack {
    if (isAir()) {
        error("air")
    }
    return modifyMeta { modifyLore(func) }
}

/**
 * 判断物品是否存在名称或特定名称
 * @param name 特定名称（留空判断是否存在任意名称）
 */
fun ItemStack.hasName(name: String? = null): Boolean {
    return if (name == null) itemMeta?.hasDisplayName() == true else itemMeta!!.displayName.contains(name)
}

/**
 * 判断物品是否存在描述或特定描述
 * @param lore 特定描述（留空判断是否存在任意描述）
 */
fun ItemStack.hasLore(lore: String? = null): Boolean {
    return if (lore == null) itemMeta?.hasLore() == true else itemMeta!!.lore.toString().contains(lore)
}

/**
 * 替换物品名称（完全替换）
 *
 * @param nameOld 文本
 * @param nameNew 文本
 * @return ItemStack
 */
fun ItemStack.replaceName(nameOld: String, nameNew: String): ItemStack {
    return replaceName(ImmutableMap.of(nameOld, nameNew))
}

/**
 * 替换物品描述（完全替换）
 *
 * @param loreOld 文本
 * @param loreNew 文本
 * @return ItemStack
 */
fun ItemStack.replaceLore(loreOld: String, loreNew: String): ItemStack {
    return replaceLore(ImmutableMap.of(loreOld, loreNew))
}

/**
 * 替换物品名称（完全替换）
 *
 * @param map  文本关系
 * @return ItemStack
 */
fun ItemStack.replaceName(map: Map<String, String>): ItemStack {
    if (hasName()) {
        val meta = itemMeta!!
        var name = meta.displayName
        map.forEach { name = name.replace(it.key, it.value) }
        meta.setDisplayName(name)
        itemMeta = meta
    }
    return this
}

/**
 * 替换物品描述（完全替换）
 *
 * @param map  文本关系
 * @return ItemStack
 */
fun ItemStack.replaceLore(map: Map<String, String>): ItemStack {
    if (hasLore()) {
        val meta = itemMeta!!
        val lore = meta.lore!!
        lore.indices.forEach { i ->
            var line = lore[i]
            map.forEach { line = line.replace(it.key, it.value) }
            lore[i] = line
        }
        meta.lore = lore
        itemMeta = meta
    }
    return this
}