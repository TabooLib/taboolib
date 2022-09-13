package taboolib.module.nms

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import taboolib.common.util.unsafeLazy

internal val nmsGeneric by unsafeLazy { nmsProxy<NMSGeneric>() }

/**
 * 获取物品NBT数据
 */
fun ItemStack.getItemTag(): ItemTag {
    if (isAir()) {
        error("ItemStack must be not null.")
    }
    return nmsGeneric.getItemTag(this)
}

/**
 * 写入物品NBT数据
 */
fun ItemStack.setItemTag(itemTag: ItemTag): ItemStack {
    if (isAir()) {
        error("ItemStack must be not null.")
    }
    return nmsGeneric.setItemTag(this, itemTag)
}

private fun ItemStack?.isAir(): Boolean {
    return this == null || type == Material.AIR || type.name.endsWith("_AIR")
}