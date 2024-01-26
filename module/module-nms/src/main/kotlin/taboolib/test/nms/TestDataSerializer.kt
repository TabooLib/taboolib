package taboolib.test.nms

import taboolib.common.Test
import taboolib.module.nms.dataSerializerBuilder

/**
 * TabooLib
 * taboolib.module.nms.test.TestDataSerializer
 *
 * @author 坏黑
 * @since 2023/8/5 00:56
 */
object TestDataSerializer : Test() {

    override fun check(): List<Result> {
        return listOf(
            sandbox("NMS:dataSerializerBuilder()") {
                dataSerializerBuilder {
                    writeUtf("test")
                    writeVarInt(1)
                }
            }
        )
    }
}