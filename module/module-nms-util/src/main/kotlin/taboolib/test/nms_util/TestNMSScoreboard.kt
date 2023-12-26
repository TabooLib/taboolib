package taboolib.test.nms_util

import taboolib.common.Isolated
import taboolib.common.Test
import taboolib.module.nms.sendScoreboard
import taboolib.platform.util.onlinePlayers

/**
 * TabooLib
 * taboolib.test.nms_util.TestNMSScoreboard
 *
 * @author 坏黑
 * @since 2023/8/5 00:56
 */
@Isolated
object TestNMSScoreboard : Test() {

    override fun check(): List<Result> {
        val player = onlinePlayers.firstOrNull()
        return if (player != null) {
            listOf(sandbox("NMSScoreboard:sendScoreboard()") { player.sendScoreboard("TEST", "123", "456") })
        } else {
            emptyList()
        }
    }
}