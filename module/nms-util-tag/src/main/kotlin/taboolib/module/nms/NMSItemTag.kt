package taboolib.module.nms

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.tabooproject.reflex.UnsafeAccess
import taboolib.common.util.unsafeLazy
import java.lang.invoke.MethodHandle

/**
 * 获取物品 [ItemTag]
 */
fun ItemStack.getItemTag(): ItemTag {
    return NMSItemTag.instance.getItemTag(validation())
}

/**
 * 将 [ItemTag] 写入物品（不会改变该物品）并返回一个新的物品
 */
fun ItemStack.setItemTag(itemTag: ItemTag): ItemStack {
    return NMSItemTag.instance.setItemTag(validation(), itemTag)
}

/**
 * 将 [ItemTagData] 转换为字符串
 */
fun ItemTagData.saveToString(): String {
    return NMSItemTag.instance.itemTagToString(this)
}

/**
 * TabooLib
 * taboolib.module.nms.NMSItemTag
 *
 * @author 坏黑
 * @since 2023/8/5 03:47
 */
abstract class NMSItemTag {

    /** 获取物品 [ItemTag] */
    abstract fun getItemTag(itemStack: ItemStack): ItemTag

    /** 将 [ItemTag] 写入物品（不会改变该物品）并返回一个新的物品 */
    abstract fun setItemTag(itemStack: ItemStack, itemTag: ItemTag): ItemStack

    /** 将 [ItemTag] 转换为字符串 */
    abstract fun itemTagToString(itemTagData: ItemTagData): String

    /** 将 [ItemTagData] 转换为 [net.minecraft.server] 下的 NBTTagCompound */
    abstract fun itemTagToNMSCopy(itemTagData: ItemTagData): Any

    /** 将 [net.minecraft.server] 下的 NBTTag 转换为 [ItemTagData] */
    abstract fun itemTagToBukkitCopy(nbtTag: Any): ItemTagData

    companion object {

        val instance by unsafeLazy {
            if (MinecraftVersion.majorLegacy >= 12005) {
                nmsProxy<NMSItemTag>("{name}Impl2")
            } else {
                nmsProxy<NMSItemTag>("{name}Impl1")
            }
        }
    }
}

/**
 * 判断物品是否为空
 */
private fun ItemStack?.validation(): ItemStack {
    if (this == null || type == Material.AIR || type.name.endsWith("_AIR")) {
        error("ItemStack must be not null.")
    } else {
        return this
    }
}