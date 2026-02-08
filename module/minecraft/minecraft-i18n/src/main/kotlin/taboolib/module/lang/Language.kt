package taboolib.module.lang

import taboolib.common.Inject
import taboolib.common.LifeCycle
import taboolib.common.OpenListener
import taboolib.common.OpenResult
import taboolib.common.io.runningResourcesInJar
import taboolib.common.platform.Awake
import taboolib.common.platform.ProxyPlayer
import taboolib.module.configuration.Configuration
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
@Awake
@Inject
object Language : OpenListener {

    /** 是否完成了首次加载 */
    private var isFirstLoaded = false

    /** 语言文件路径 */
    var path = "lang"

    /**
     * 语言文件释放路径
     */
    var releasePath = "plugins/{0}/lang/{1}"

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
    var enableSimpleComponent = false

    /**
     * 是否启用文件监听
     * 于 2024/11/20 起，不再默认启用
     */
    var enableFileWatcher = false

    /**
     * 重设根目录
     * 传入 "i18n" 会将语言文件写入 "i18n/{0}/{1}" 目录
     */
    fun setRoot(path: String) {
        releasePath = "$path/{0}/{1}"
    }

    /** 添加新的语言文件 */
    fun addLanguage(vararg code: String) {
        languageCode += code
        // 如果已经完成了首次加载，则立刻重载语言文件
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
        if (!isFirstLoaded) {
            // 加载语言文件类型
            runningResourcesInJar.keys
                .filter { it.startsWith("$path/") }
                .filter { Configuration.getTypeFromExtensionOrNull(it.substringAfterLast('.')) != null }
                .forEach {
                    languageCode += it.substringAfterLast('/').substringBeforeLast('.')
                }
            // 加载颜色字符模块
            if (ColorTransfer.isSupported && !textTransfer.contains(ColorTransfer)) {
                textTransfer += ColorTransfer
            }
        }
        // 加载语言文件
        isFirstLoaded = true
        languageFile.clear()
        languageFile.putAll(ResourceReader(Language::class.java).files)
    }

    override fun call(name: String, data: Array<out Any>?): OpenResult {
        return when (name) {
            "taboolib:language_reload" -> OpenResult.successful(reload())
            "taboolib:language_code" -> OpenResult.successful(languageCode)
            "taboolib:language_file" -> OpenResult.successful(languageFile.keys)
            else -> OpenResult.failed()
        }
    }
}