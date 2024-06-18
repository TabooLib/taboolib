package taboolib.test

import taboolib.common.Test
import taboolib.module.nms.LocaleI18n

/**
 * TabooLib
 * taboolib.module.nms.test.TestLocaleI18n
 *
 * @author 坏黑
 * @since 2023/8/5 00:56
 */
object TestLocaleI18n : Test() {

    override fun check(): List<Result> {
        return listOf(
            sandbox("I18n:localeFiles") {
                val support = LocaleI18n.supportedLanguage
                val size = LocaleI18n.localeFiles.size
                if (size != support.size) error("$size (lose: ${LocaleI18n.supportedLanguage.filter { LocaleI18n.localeFiles[it] == null }})")
            }
        )
    }
}