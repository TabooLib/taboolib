package taboolib.module.lang

import taboolib.common.Inject
import taboolib.common.LifeCycle
import taboolib.common.io.runningResourcesInJar
import taboolib.common.platform.Awake
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.ProxyPlayer
import taboolib.module.chat.HexColor
import taboolib.module.chat.colored
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.Type
import taboolib.module.lang.event.PlayerSelectLocaleEvent
import taboolib.module.lang.event.SystemSelectLocaleEvent
import taboolib.module.lang.gameside.TypeActionBar
import taboolib.module.lang.gameside.TypeCommand
import taboolib.module.lang.gameside.TypeSound
import taboolib.module.lang.gameside.TypeTitle
import java.util.*

/**
 * TabooLib
 * taboolib.module.lang.Language
 *
 * @author sky
 * @since 2021/6/18 10:43 下午
 */
@Inject
object Language {

    /** 是否完成了首次加载 */
    private var isFirstLoaded = false

    /** 语言文件路径 */
    var path = "lang"

    /** 默认语言文件 */
    var default = "zh_CN"

    /** 文本转换 */
    val textTransfer = ArrayList<TextTransfer>()

    /** 语言文件缓存 */
    val languageFile = HashMap<String, LanguageFile>()

    /** 语言文件代码 */
    val languageCode = HashSet<String>()

    /** 语言文件代码转换 */
    val languageCodeTransfer = hashMapOf(
        "zh_hans_cn" to "zh_CN",
        "zh_hant_cn" to "zh_TW",
        "en_ca" to "en_US",
        "en_au" to "en_US",
        "en_gb" to "en_US",
        "en_nz" to "en_US"
    )

    /** 语言文件类型 */
    @Suppress("SpellCheckingInspection")
    val languageType = hashMapOf(
        "text" to TypeText::class.java,
        "raw" to TypeJson::class.java,
        "json" to TypeJson::class.java,
        "title" to TypeTitle::class.java,
        "sound" to TypeSound::class.java,
        "command" to TypeCommand::class.java,
        "actionbar" to TypeActionBar::class.java,
        "action_bar" to TypeActionBar::class.java,
        "simpletext" to TypeSimpleText::class.java,
        "simple_text" to TypeSimpleText::class.java,
    )

    /** 是否在语言文件中启用 SimpleComponent 格式化 */
    val enableSimpleComponent = false

    /** 添加新的语言文件 */
    fun addLanguage(vararg code: String) {
        languageCode += code
        if (isFirstLoaded) {
            reload()
        }
    }

    /** 获取玩家语言 */
    fun getLocale(player: ProxyPlayer): String {
        return PlayerSelectLocaleEvent(player, languageCodeTransfer[player.locale.lowercase()] ?: player.locale).run {
            call()
            locale
        }
    }

    /** 获取控制台语言 */
    fun getLocale(): String {
        val code = Locale.getDefault().toLanguageTag().replace("-", "_").lowercase()
        return SystemSelectLocaleEvent(languageCodeTransfer[code] ?: code).run {
            call()
            locale
        }
    }

    @Awake(LifeCycle.INIT)
    fun reload() {
        // 加载语言文件类型
        runningResourcesInJar.keys.filter { it.startsWith("$path/") }.filter {
            Configuration.getTypeFromExtension(it.substringAfterLast('.')) in Type.values()
        }.forEach {
            languageCode += it.substringAfterLast('/').substringBeforeLast('.')
        }
        // 加载颜色字符模块
        try {
            HexColor.translate("")
            textTransfer += object : TextTransfer {
                override fun translate(sender: ProxyCommandSender, source: String, vararg args: Any): String {
                    return source.colored()
                }
            }
        } catch (_: NoClassDefFoundError) {
        }
        // 加载语言文件
        isFirstLoaded = true
        languageFile.clear()
        languageFile.putAll(ResourceReader(Language::class.java).files)
    }
}