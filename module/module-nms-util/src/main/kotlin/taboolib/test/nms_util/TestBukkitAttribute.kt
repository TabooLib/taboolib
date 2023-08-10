package taboolib.test.nms_util

import taboolib.common.Isolated
import taboolib.common.Test
import taboolib.module.nms.BukkitAttribute

/**
 * TabooLib
 * taboolib.test.nms_util.TestBukkitAttribute
 *
 * @author 坏黑
 * @since 2023/8/5 00:56
 */
@Isolated
object TestBukkitAttribute : Test() {

    override fun check(): List<Result> {
        return listOf(
            sandbox("BukkitAttribute:toNMS()") {
                BukkitAttribute.values().forEach { it.toNMS() }
            },
            sandbox("BukkitAttribute:toBukkit()") {
                BukkitAttribute.values().forEach { it.toBukkit() }
            }
        )
    }
}