package taboolib.module.nms.test

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import taboolib.common.Test
import taboolib.module.chat.Components
import taboolib.module.nms.setDisplayNameComponent
import taboolib.module.nms.setLoreComponents

/**
 * TabooLib
 * taboolib.test.nms_util.TestNMSItemRaw
 *
 * @author 坏黑
 * @since 2024/9/8 00:56
 */
object TestNMSItemRaw : Test() {

    override fun check(): List<Result> {
        return listOf(
            sandbox("NMSItemRaw:setDisplayName()") {
                ItemStack(Material.STONE).itemMeta!!.setDisplayNameComponent(Components.text("啥比"))
            },
            sandbox("NMSItemRaw:setLore()") {
                ItemStack(Material.STONE).itemMeta!!.setLoreComponents(listOf(Components.text("啥比")))
            }
        )
    }
}