package taboolib.module.nms.test

import taboolib.common.Test
import taboolib.module.nms.MinecraftLanguage

/**
 * TabooLib
 * taboolib.module.nms.test.TestLocaleI18n
 *
 * @author 坏黑
 * @since 2023/8/5 00:56
 */
object TestMinecraftLanguage : Test() {

    override fun check(): List<Result> {
        return listOf(
            sandbox("NMS:MinecraftLanguage") {
                val support = MinecraftLanguage.supportedLanguage
                val size = MinecraftLanguage.files.size
                if (size != support.size) error("$size (lose: ${MinecraftLanguage.supportedLanguage.filter { MinecraftLanguage.files[it] == null }})")
            }
        )
    }
}