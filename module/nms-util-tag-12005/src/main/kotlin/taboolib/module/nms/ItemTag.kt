package taboolib.module.nms

import org.bukkit.inventory.ItemStack

/**
 * TabooLib
 * taboolib.module.nms.ItemTag
 *
 * @author mical
 * @date 2024/9/7 13:35
 */
class ItemTag12005 : ItemTag {

    constructor() : super()

    constructor(map: Map<String, ItemTagData>) : super(map)

    /**
     * 在 1.20.5 上将完整的 [ItemTag] 写入物品
     */
    override fun saveTo(item: ItemStack) {
        val newItem = item.setItemTag(this)
        item.type = newItem.type
        item.amount = newItem.amount
        item.durability = newItem.durability
        item.itemMeta = newItem.itemMeta
    }
}