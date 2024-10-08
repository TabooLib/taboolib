package taboolib.module.nms.test

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
object TestNMSScoreboard : Test() {

    override fun check(): List<Result> {
        val player = onlinePlayers.firstOrNull()
        return if (player != null) {
            listOf(
                sandbox("NMSScoreboard:sendScoreboard()") { player.sendScoreboard("TEST", "123", "456") },
                sandbox("NMSScoreboard:sendScoreboard()") { player.sendScoreboard("TEST", "123") },
                sandbox("NMSScoreboard:sendScoreboard()") { player.sendScoreboard("TEST", "123", "456", "789") },
            )
        } else {
            emptyList()
        }
    }
}