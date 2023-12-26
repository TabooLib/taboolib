package taboolib.test.nms_util

import org.bukkit.Material
import taboolib.common.Isolated
import taboolib.common.Test
import taboolib.module.nms.sendToast
import taboolib.platform.util.onlinePlayers

/**
 * TabooLib
 * taboolib.test.nms_util.TestNMSToast
 *
 * @author 坏黑
 * @since 2023/8/5 00:56
 */
@Isolated
object TestNMSToast : Test() {

    override fun check(): List<Result> {
        val player = onlinePlayers.firstOrNull()
        return if (player != null) {
            listOf(sandbox("NMSToast:sendToast()") { player.sendToast(Material.DIAMOND, "测试") })
        } else {
            emptyList()
        }
    }
}