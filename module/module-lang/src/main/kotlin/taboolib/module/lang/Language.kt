package taboolib.module.lang

import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.ProxyPlayer
import taboolib.common.platform.SkipTo
import taboolib.common.platform.function.getJarFile
import taboolib.common.platform.function.info
import taboolib.common.platform.function.pluginId
import taboolib.module.chat.HexColor
import taboolib.module.chat.colored
import taboolib.module.lang.event.PlayerSelectLocaleEvent
import taboolib.module.lang.event.SystemSelectLocaleEvent
import java.util.*
import java.util.jar.JarFile

/**
 * TabooLib
 * taboolib.module.lang.Language
 *
 * @author sky
 * @since 2021/6/18 10:43 下午
 */
@SkipTo(LifeCycle.INIT)
object Language {

    private var firstLoaded = false

    var default = "zh_CN"

    val textTransfer = ArrayList<TextTransfer>()

    val languageFile = HashMap<String, LanguageFile>()

    val languageCode = HashSet<String>()

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
        "json" to TypeJson::class.java,
        "title" to TypeTitle::class.java,
        "sound" to TypeSound::class.java,
        "command" to TypeCommand::class.java,
        "actionbar" to TypeActionBar::class.java
    )

    init {
        // 加载语言文件类型
        JarFile(getJarFile()).use { jar ->
            jar.entries().iterator().forEachRemaining {
                if (it.name.startsWith("lang/") && it.name.endsWith(".yml")) {
                    languageCode += it.name.substringAfter('/').substringBeforeLast('.')
                }
            }
        }
        // 加载颜色字符模块
        try {
            HexColor.translate("")
            textTransfer += object : TextTransfer {
                override fun translate(sender: ProxyCommandSender, source: String): String {
                    return source.colored()
                }
            }
        } catch (_: NoClassDefFoundError) {
        }
    }

    fun addLanguage(vararg code: String) {
        languageCode += code
        if (firstLoaded) {
            reload()
        }
    }

    fun getLocale(player: ProxyPlayer): String {
        return PlayerSelectLocaleEvent(player, languageCodeTransfer[player.locale.lowercase()] ?: player.locale).run {
            call()
            locale
        }
    }

    fun getLocale(): String {
        val code = Locale.getDefault().toLanguageTag().replace("-", "_").lowercase()
        return SystemSelectLocaleEvent(languageCodeTransfer[code] ?: code).run {
            call()
            locale
        }
    }

    @Awake(LifeCycle.INIT)
    fun reload() {
        // 加载语言文件
        firstLoaded = true
        languageFile.clear()
        languageFile.putAll(ResourceReader(Language::class.java).files)
    }
}