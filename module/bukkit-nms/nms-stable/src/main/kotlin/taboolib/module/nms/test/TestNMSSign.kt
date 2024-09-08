package taboolib.module.nms.test

import org.bukkit.Bukkit
import taboolib.common.Test
import taboolib.common.platform.function.info
import taboolib.module.nms.inputSign

/**
 * TabooLib
 * taboolib.test.nms_util.TestNMSSign
 *
 * @author 坏黑
 * @since 2024/9/8 00:56
 */
object TestNMSSign : Test() {

    override fun check(): List<Result> {
        val player = Bukkit.getOnlinePlayers().firstOrNull()
        return if (player != null) {
            listOf(sandbox("NMSSign:inputSign()") {
                player.inputSign(arrayOf("我是啥比")) { info("输入 ${it.contentToString()}") }
            })
        } else {
            emptyList()
        }
    }
}