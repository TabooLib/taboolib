package taboolib.module.lang

import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.ProxyPlayer
import taboolib.module.lang.event.ConsoleSelectLocaleEvent
import taboolib.module.lang.event.PlayerSelectLocaleEvent
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

    val textTransfer = arrayListOf(TextTransferKether)

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

    val languageType = hashMapOf(
        "text" to TypeText::class.java,
        "title" to TypeText::class.java,
        "sound" to TypeSound::class.java,
        "actionbar" to TypeActionBar::class.java
    )

    fun getLocale(player: ProxyPlayer): String {
        return PlayerSelectLocaleEvent(player, languageCodeTransfer[player.locale] ?: player.locale).run {
            call()
            locale
        }
    }

    fun getLocale(): String {
        val code = Locale.getDefault().toLanguageTag().replace("-", "_")
        return ConsoleSelectLocaleEvent(languageCodeTransfer[code] ?: code).run {
            call()
            locale
        }
    }

    fun String.translate(sender: ProxyCommandSender): String {
        var s = this
        textTransfer.forEach { s = it.translate(sender, s) }
        return s
    }
}