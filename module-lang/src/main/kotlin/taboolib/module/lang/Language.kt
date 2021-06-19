package taboolib.module.lang

import org.intellij.lang.annotations.Language
import taboolib.common.platform.ProxyPlayer
import java.util.*
import kotlin.collections.HashMap

/**
 * TabooLib
 * taboolib.module.lang.Language
 *
 * @author sky
 * @since 2021/6/18 10:43 下午
 */
object Language {

    val languageFile = HashMap<String, LanguageFile>()

    val languageCode = arrayListOf("zh_CN", "en_US")

    val languageCodeTransfer = hashMapOf(
        "zh_hans_cn" to "zh_CN",
        "zh_hant_cn" to "zh_TW",
        "en_ca" to "en_US",
        "en_au" to "en_US",
        "en_gb" to "en_US",
        "en_nz" to "en_US"
    )

    fun getLocale(player: ProxyPlayer): String {
        return languageCodeTransfer[player.locale] ?: player.locale
    }

    fun getLocale(): String {
        val code = Locale.getDefault().toLanguageTag().replace("-", "_")
        return languageCodeTransfer[code] ?: code
    }
}