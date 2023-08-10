package taboolib.test.nms_util

import org.bukkit.Bukkit
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import taboolib.common.Isolated
import taboolib.common.Test
import taboolib.common.UnsupportedVersionException
import taboolib.module.nms.sendRawActionBar
import taboolib.module.nms.sendRawTitle
import taboolib.module.nms.setRawTitle
import taboolib.platform.util.onlinePlayers

/**
 * TabooLib
 * taboolib.test.nms_util.TestNMSMessage
 *
 * @author 坏黑
 * @since 2023/8/5 00:56
 */
@Isolated
object TestNMSMessage : Test() {

    override fun check(): List<Result> {
        val player = onlinePlayers.firstOrNull()
        return if (player != null) {
            listOf(
                sandbox("NMSMessage:sendRawTitle()") { player.sendRawTitle("{\"text\":\"测试\"}", "{\"text\":\"测试\"}") },
                sandbox("NMSMessage:sendRawActionBar()") { player.sendRawActionBar("{\"text\":\"测试\"}") },
                sandbox("NMSMessage:setRawTitle()") {
                    try {
                        Bukkit.createBossBar("", BarColor.PURPLE, BarStyle.SOLID).setRawTitle("{\"text\":\"测试\"}")
                    } catch (ex: NoClassDefFoundError) {
                        throw UnsupportedVersionException()
                    }
                }
            )
        } else {
            emptyList()
        }
    }
}