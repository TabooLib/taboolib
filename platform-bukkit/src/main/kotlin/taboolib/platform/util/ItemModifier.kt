@file:Isolated

package taboolib.platform.util

import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import taboolib.common.Isolated
import taboolib.library.xseries.XMaterial
import taboolib.library.xseries.XMaterialUtil

fun ItemStack?.isAir(): Boolean {
    return this == null || XMaterialUtil.isAir(XMaterial.matchXMaterial(this))
}

fun ItemStack?.isNotAir(): Boolean {
    return !isAir()
}

fun ItemStack.modifyMeta(func: ItemMeta.() -> Unit): ItemStack {
    return also { itemMeta = itemMeta!!.also(func) }
}

fun ItemMeta.modifyLore(func: MutableList<String>.() -> Unit): ItemMeta {
    return also { lore = (lore ?: ArrayList<String>()).also(func) }
}

fun ItemStack.modifyLore(func: MutableList<String>.() -> Unit): ItemStack {
    return modifyMeta { modifyLore(func) }
}