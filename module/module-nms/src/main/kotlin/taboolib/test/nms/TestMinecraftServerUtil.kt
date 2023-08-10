package taboolib.test.nms

import taboolib.common.Isolated
import taboolib.common.Test
import taboolib.module.nms.minecraftServerObject
import taboolib.module.nms.nmsClass
import taboolib.module.nms.obcClass

/**
 * TabooLib
 * taboolib.module.nms.test.TestMinecraftServerUtil
 *
 * @author 坏黑
 * @since 2023/8/5 00:56
 */
@Isolated
object TestMinecraftServerUtil : Test() {

    override fun check(): List<Result> {
        return listOf(
                sandbox("NMS:minecraftServerObject") { minecraftServerObject },
                sandbox("NMS:obcClass(String)") { obcClass("CraftServer") },
                sandbox("NMS:nmsClass(String)") { nmsClass("MinecraftServer") },
        )
    }
}