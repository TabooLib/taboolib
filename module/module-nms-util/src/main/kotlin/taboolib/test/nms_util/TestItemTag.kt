package taboolib.test.nms_util

import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import taboolib.common.Isolated
import taboolib.common.Test
import taboolib.module.nms.ItemTagSerializer
import taboolib.module.nms.getItemTag
import taboolib.platform.util.buildItem

/**
 * TabooLib
 * taboolib.test.nms_util.TestItemTag
 *
 * @author 坏黑
 * @since 2023/8/5 00:56
 */
@Isolated
object TestItemTag : Test() {

    override fun check(): List<Result> {
        val item = buildItem(Material.DIAMOND_SWORD) {
            name = "你妈死了"
            lore += "测试"
            enchants[Enchantment.DAMAGE_ALL] = 1
        }
        return listOf(
            sandbox("ItemTag:getItemTag()") { item.getItemTag() },
            sandbox("ItemTag:getItemTag().getDeep()") { item.getItemTag().getDeep("display.Name") != null },
            sandbox("ItemTagSerializer:serializeTag()") { ItemTagSerializer.serializeTag(item.getItemTag()) },
            sandbox("ItemTagSerializer:deserializeTag()") { ItemTagSerializer.deserializeTag(ItemTagSerializer.serializeTag(item.getItemTag())) },
        )
    }
}