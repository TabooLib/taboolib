package taboolib.module.nms.test

import taboolib.common.Isolated
import taboolib.common.Test
import taboolib.module.nms.LocaleI18n

/**
 * TabooLib
 * taboolib.module.nms.test.TestLocaleI18n
 *
 * @author 坏黑
 * @since 2023/8/5 00:56
 */
@Isolated
object TestLocaleI18n : Test() {

    override fun check(): List<Result> {
        return listOf(
            sandbox("I18n:localeFiles") { if (LocaleI18n.localeFiles.size != 4) throw IllegalStateException() }
        )
    }
}