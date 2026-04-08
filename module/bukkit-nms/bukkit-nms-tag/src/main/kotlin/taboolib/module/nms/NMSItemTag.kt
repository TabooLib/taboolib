package taboolib.module.nms

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import taboolib.common.util.t
import taboolib.common.util.unsafeLazy

/**
 * 获取物品 [ItemTag]
 *
 * @param onlyCustom 是否仅包含自定义数据（详见 1.20.5+ NBT 改动，在 1.20.4 及以下版本此参数无效）
 */
fun ItemStack.getItemTag(onlyCustom: Boolean = true): ItemTag {
    return NMSItemTag.instance.getItemTag(validation(), onlyCustom)
}

/**
 * 将 [ItemTag] 写入物品（不会改变该物品）并返回一个新的物品
 *
 * @param itemTag 要写入的 [ItemTag]
 * @param onlyCustom 是否仅包含自定义数据（详见 1.20.5+ NBT 改动，在 1.20.4 及以下版本此参数无效）
 */
fun ItemStack.setItemTag(itemTag: ItemTag, onlyCustom: Boolean = true): ItemStack {
    return NMSItemTag.instance.setItemTag(validation(), itemTag, onlyCustom)
}

/**
 * 将 [ItemTagData] 转换为字符串
 */
fun ItemTagData.saveToString(): String {
    return NMSItemTag.instance.itemTagToString(this)
}

/**
 * 将物品转换为原版 Json 形式
 * @return 转换后的 Json 字符串，可插入 Tellraw 信息中
 */
fun ItemStack.toMinecraftJson(): String {
    return NMSItemTag.instance.toMinecraftJson(this)
}

/**
 * 设置物品可破坏方块列表
 *
 * @param blocks 列表，方块名称，如 "minecraft:stone"
 * @return 添加成功返回新的物品，否则返回原物品
 */
fun ItemStack.setItemCanBreak(blocks: List<String>): ItemStack {
    return NMSItemTag.instance.setItemCanBreak(validation(), blocks)
}

/**
 * 设置物品可放置方块列表
 *
 * @param blocks 列表，方块名称，如 "minecraft:stone"
 * @return 添加成功返回新的物品，否则返回原物品
 */
fun ItemStack.setItemCanPlaceOn(blocks: List<String>): ItemStack {
    return NMSItemTag.instance.setItemCanPlaceOn(validation(), blocks)
}

/**
 * 移除物品可破坏方块列表
 *
 * @return 移除成功返回新的物品，否则返回原物品
 */
fun ItemStack.removeItemCanBreak(): ItemStack {
    return NMSItemTag.instance.removeItemCanBreak(validation())
}

/**
 * 移除物品可放置方块列表
 *
 * @return 移除成功返回新的物品，否则返回原物品
 */
fun ItemStack.removeItemCanPlaceOn(): ItemStack {
    return NMSItemTag.instance.removeItemCanPlaceOn(validation())
}

/**
 * 检查物品是否设置了可破坏方块组件
 *
 * @return 如果物品包含 can_break 数据组件则返回 true，否则返回 false
 */
fun ItemStack.hasItemCanBreak(): Boolean {
    return NMSItemTag.instance.hasItemCanBreak(validation())
}

/**
 * 检查物品是否设置了可放置方块组件
 *
 * @return 如果物品包含 can_place_on 数据组件则返回 true，否则返回 false
 */
fun ItemStack.hasItemCanPlaceOn(): Boolean {
    return NMSItemTag.instance.hasItemCanPlaceOn(validation())
}

/**
 * TabooLib
 * taboolib.module.nms.NMSItemTag
 *
 * @author 坏黑
 * @since 2023/8/5 03:47
 */
abstract class NMSItemTag {

    /** 生成适配版本的 [ItemTag] **/
    abstract fun newItemTag(): ItemTag

    /** 将 Bukkit [ItemStack] 转换为 NMS [ItemStack] */
    abstract fun getNMSCopy(itemStack: ItemStack): Any

    /** 将 NMS [ItemStack] 转换为 Bukkit [ItemStack] */
    abstract fun getBukkitCopy(itemStack: Any): ItemStack

    /** 获取物品 [ItemTag] */
    abstract fun getItemTag(itemStack: ItemStack, onlyCustom: Boolean): ItemTag

    /** 将 [ItemTag] 写入物品（不会改变该物品）并返回一个新的物品 */
    abstract fun setItemTag(itemStack: ItemStack, itemTag: ItemTag, onlyCustom: Boolean): ItemStack

    /** 将 [ItemTag] 转换为字符串 */
    abstract fun itemTagToString(itemTagData: ItemTagData): String

    /** 将 [ItemTagData] 转换为 [net.minecraft.server] 下的 NBTTagCompound */
    abstract fun itemTagToNMSCopy(itemTagData: ItemTagData): Any

    /** 将 [net.minecraft.server] 下的 NBTTag 转换为 [ItemTagData] */
    abstract fun itemTagToBukkitCopy(nbtTag: Any): ItemTagData

    /**
     * 设置物品可破坏方块列表
     *
     * @param blocks 列表，方块名称，如 "minecraft:stone"
     * @return 设置成功返回新的物品，否则返回原物品
     */
    abstract fun setItemCanBreak(itemStack: ItemStack, blocks: List<String>): ItemStack

    /**
     * 设置物品可放置方块列表
     *
     * @param blocks 列表，方块名称，如 "minecraft:stone"
     * @return 设置成功返回新的物品，否则返回原物品
     */
    abstract fun setItemCanPlaceOn(itemStack: ItemStack, blocks: List<String>): ItemStack

    /**
     * 检查物品是否设置了可破坏方块组件
     *
     * @param itemStack 要检查的物品
     * @return 如果物品包含 can_break 数据组件则返回 true，否则返回 false
     */
    abstract fun hasItemCanBreak(itemStack: ItemStack): Boolean

    /**
     * 检查物品是否设置了可放置方块组件
     *
     * @param itemStack 要检查的物品
     * @return 如果物品包含 can_place_on 数据组件则返回 true，否则返回 false
     */
    abstract fun hasItemCanPlaceOn(itemStack: ItemStack): Boolean

    /**
     * 移除物品可破坏方块列表
     *
     * @param itemStack 要移除物品的方块列表的物品
     * @return 移除成功返回新的物品，否则返回原物品
     */
    abstract fun removeItemCanBreak(itemStack: ItemStack): ItemStack

    /**
     * 移除物品可放置方块列表
     *
     * @param itemStack 要移除物品的方块列表的物品
     * @return 移除成功返回新的物品，否则返回原物品
     */
    abstract fun removeItemCanPlaceOn(itemStack: ItemStack): ItemStack

    /**
     * 将物品转换为原版 Json 形式，可插入 Tellraw 信息中。
     * 和 ItemTag#toJson 不同，不含类型信息。
     *
     * 在 1.20.5 以下的版本，不含物品基本信息。
     */
    abstract fun toMinecraftJson(itemStack: ItemStack): String

    /**
     * 将原版 Json 转换为物品。
     * 此方法不能接受 ItemTag#toJson 的结果。
     *
     * 在 1.20.5 以下的版本，由于 [toMinecraftJson] 不含物品基本信息，因此默认为 STONE 类型。
     */
    abstract fun fromMinecraftJson(json: String): ItemStack?

    companion object {

        val versionId = MinecraftVersion.versionId

        val instance by unsafeLazy {
            when {
                MinecraftVersion.isUnobfuscated -> nmsProxy<NMSItemTag>("{name}Impl2")
                versionId >= 12005 -> nmsProxy<NMSItemTag>("{name}Impl")
                else -> nmsProxy<NMSItemTag>("{name}Legacy")
            }
        }

        /**
         * 获取 [ItemStack] 的 NMS 副本
         */
        fun asNMSCopy(item: ItemStack): Any {
            return instance.getNMSCopy(item)
        }

        /**
         * 获取 NMS 物品的 Bukkit 副本
         */
        fun asBukkitCopy(item: Any): ItemStack {
            return instance.getBukkitCopy(item)
        }
    }
}

/**
 * 判断物品是否为空
 */
private fun ItemStack?.validation(): ItemStack {
    if (this == null || type == Material.AIR || type.name.endsWith("_AIR")) {
        error(
            """
                ItemStack 不能为空。
                ItemStack must be not null.
            """.t()
        )
    } else {
        return this
    }
}
