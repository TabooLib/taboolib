package taboolib.module.nms.test

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import taboolib.common.Test
import taboolib.common.platform.function.info
import taboolib.platform.util.toNMSKeyAndItemData

/**
 * TabooLib
 * taboolib.test.nms_util.TestNMSSign
 *
 * @author 坏黑
 * @since 2024/9/8 00:56
 */
object TestTellrawJson : Test() {

    override fun check(): List<Result> {
        return listOf(sandbox("TellrawJson:toNMSKeyAndItemData()") {
            info(ItemStack(Material.STONE).toNMSKeyAndItemData())
        })
    }
}