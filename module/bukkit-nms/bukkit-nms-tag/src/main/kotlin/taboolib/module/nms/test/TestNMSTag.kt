package taboolib.module.nms.test

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import taboolib.common.Test
import taboolib.module.nms.ItemTag
import taboolib.module.nms.getItemTag
import taboolib.module.nms.setItemTag
import taboolib.platform.util.modifyMeta

/**
 * TabooLib
 * taboolib.module.nms.test.TestNMS
 *
 * @author 坏黑
 * @since 2024/7/21 17:27
 */
object TestNMSTag : Test() {

    override fun check(): List<Result> {
        val result = arrayListOf<Result>()
        var itemTag: ItemTag? = null
        result += sandbox("NMS:getItemTag") { itemTag = item().getItemTag() }
        result += sandbox("NMS:setItemTag") { item().setItemTag(itemTag ?: ItemTag()) }
        return result
    }

    fun item(): ItemStack {
        return ItemStack(Material.STONE).modifyMeta<ItemMeta> {
            setDisplayName("1")
            lore = listOf("2")
        }
    }
}