package taboolib.module.nms

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.bukkit.entity.Player
import org.tabooproject.reflex.Reflex.Companion.getProperty
import taboolib.common.Inject
import taboolib.common.LifeCycle
import taboolib.common.PrimitiveIO
import taboolib.common.io.digest
import taboolib.common.io.newFile
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.function.info
import taboolib.common.util.MatrixList
import taboolib.common.util.t
import taboolib.platform.bukkit.Exchanges
import java.io.File
import java.net.URL
import java.util.*

/**
 * 获取玩家对应的语言文件
 */
fun Player.getMinecraftLanguageFile(): MinecraftLanguage.LanguageFile? {
    val locale = try {
        locale
    } catch (_: NoSuchMethodError) {
        getProperty("handle/locale")!!
    }
    return MinecraftLanguage.getLanguageFile(locale) ?: MinecraftLanguage.getDefaultLanguageFile()
}

/**
 * TabooLib
 * taboolib.module.nms.LocaleI18n
 *
 * @author 坏黑
 * @since 2023/8/6 02:44
 */
@Inject
@PlatformSide(Platform.BUKKIT)
object MinecraftLanguage {

    /**
     * 语言文件
     */
    interface LanguageFile {

        /** 原始文件 */
        val sourceFile: File

        /** 容器 */
        val container: Any

        /** 获取语言文件值 */
        operator fun get(path: String): String?

        /** 获取语言文件值 */
        operator fun get(localeKey: LanguageKey) = if (localeKey.extra != null) get(localeKey.path) + " " + get(localeKey.extra) else get(localeKey.path)

        /**
         * 1.8 .. 1.12 版本语言文件格式
         */
        class FormatProperties(override val sourceFile: File, override val container: Properties) : LanguageFile {

            constructor(file: File) : this(file, Properties().apply { load(file.reader()) })

            override operator fun get(path: String): String? {
                return container.getProperty(path)
            }
        }

        /**
         * 1.13 至今语言文件格式
         */
        class FormatJson(override val sourceFile: File, override val container: JsonObject) : LanguageFile {

            constructor(file: File) : this(file, JsonParser().parse(file.readText()).asJsonObject)

            override operator fun get(path: String): String? {
                return container[path]?.asString
            }
        }
    }

    /**
     * 语言文件节点
     *
     * @param type 类型
     * @param path 节点
     * @param extra 额外信息（在低版本中表现为生成蛋的类型）
     */
    data class LanguageKey(val type: Type, val path: String, val extra: String? = null) {

        enum class Type {

            /** 正常的 */
            NORMAL,

            /** 特殊的 */
            SPECIAL,

            /** 默认的 */
            DEFAULT
        }

        override fun toString(): String {
            return "[$type] " + if (extra == null) path else "$path ($extra)"
        }
    }

    /** 资源文件地址 */
    var resourceUrl = "https://resources.download.minecraft.net"

    /** 支持的语言文件 */
    val supportedLanguage = arrayListOf("zh_cn", "zh_tw", "en_gb")

    /** 语言文件 */
    val files = hashMapOf<String, LanguageFile>()

    /**
     * 获取语言文件
     */
    fun getLanguageFile(locale: String): LanguageFile? {
        return if (locale == "en_us") files["en_gb"] else files[locale]
    }

    /**
     * 获取默认语言文件
     */
    fun getDefaultLanguageFile(): LanguageFile? {
        return files["zh_cn"]
    }

    @Awake(LifeCycle.INIT)
    private fun init() {
        // 访问内存中的共享文件
        if (loadFilesFromExchange()) {
            return
        }
        // 检查本地文件是否有效
        if (!checkFiles()) {
            info(
                """
                    正在下载 Minecraft 语言文件 ...
                    Downloading Minecraft language files ...
                """.t()
            )
            downloadFiles()
        }
        // 加载本地文件
        loadFiles()
        saveExchanges()
    }

    /** 从 Exchanges 空间中获取语言文件，跳过 I/O 过程 */
    private fun loadFilesFromExchange(): Boolean {
        if (Exchanges.MINECRAFT_LANGUAGE in Exchanges) {
            val map = Exchanges.get<MatrixList<Any>>(Exchanges.MINECRAFT_LANGUAGE)
            if (MinecraftVersion.isHigher(MinecraftVersion.V1_12)) {
                map.forEach { files[it[0] as String] = LanguageFile.FormatJson(it[1] as File, it[2] as JsonObject) }
            } else {
                map.forEach { files[it[0] as String] = LanguageFile.FormatProperties(it[1] as File, it[2] as Properties) }
            }
            return true
        }
        return false
    }

    /** 将已加载的语言文件写入到 Exchange 空间 */
    private fun saveExchanges() {
        Exchanges[Exchanges.MINECRAFT_LANGUAGE] = files.map { (k, v) -> listOf(k, v.sourceFile, v.container) }
    }

    /** 检查语言文件 */
    private fun checkFiles(): Boolean {
        return getFiles().size == supportedLanguage.size
    }

    /** 下载语言文件 */
    private fun downloadFiles() {
        // region
        val manifest = readJson("https://launchermeta.mojang.com/mc/game/version_manifest.json")
        for (ver in manifest.getAsJsonArray("versions")) {
            if (ver.asJsonObject["id"].asString == MinecraftVersion.runningVersion) {
                // 获取版本信息
                val versionObject = readJson(ver.asJsonObject["url"].asString)
                // 获取资源信息
                val assetIndexObject = readJson(versionObject["assetIndex"].asJsonObject["url"].asString)
                // 下载语言文件
                val objects = assetIndexObject["objects"].asJsonObject
                supportedLanguage.forEach { language ->
                    // 不同版本语言文件路径不同，可能存在的形式：
                    // - minecraft/lang/zh_CN.lang
                    // - minecraft/lang/zh_cn.lang << 截止到 1.12 版本，为 properties 格式
                    // - minecraft/lang/zh_cn.json
                    val names = arrayOf(
                        "minecraft/lang/${language.substringBefore('_')}_${language.substringAfter('_').uppercase()}.lang",
                        "minecraft/lang/$language.lang",
                        "minecraft/lang/$language.json",
                    )
                    for (name in names) {
                        if (objects[name] != null) {
                            val langHash = objects[name].asJsonObject["hash"].asString
                            // 检查文件是否有效
                            val file = getFile(language)
                            if (file.exists() && file.length() > 0) {
                                break
                            }
                            PrimitiveIO.println(
                                """
                                    正在下载语言文件 ... $language
                                    Downloading language ... $language
                                """.t()
                            )
                            // 获取语言文件文本并写入本地文件
                            newFile(file).writeText(URL("$resourceUrl/${langHash.substring(0, 2)}/$langHash").readText())
                            break
                        } else {
                            PrimitiveIO.warning(
                                """
                                    未能找到 Minecraft 语言文件 ... $language
                                    Minecraft language not found ... $language
                                """.t()
                            )
                        }
                    }
                }
                return
            }
        }
        PrimitiveIO.warning(
            """
                未能找到 Minecraft 语言文件。
                Minecraft language not found.
            """.t()
        )
        // endregion
    }

    /** 加载语言文件 */
    private fun loadFiles() {
        files += getFiles().mapValues { (_, v) -> if (MinecraftVersion.isHigher(MinecraftVersion.V1_12)) LanguageFile.FormatJson(v) else LanguageFile.FormatProperties(v) }
    }

    private fun readJson(url: String): JsonObject {
        return JsonParser().parse(URL(url).readText()).asJsonObject
    }

    private fun getFile(locale: String): File {
        val hash = (MinecraftVersion.runningVersion + locale.lowercase()).digest("sha-1")
        return File("assets/${hash.substring(0, 2)}/$hash")
    }

    private fun getFiles(): Map<String, File> {
        val map = HashMap<String, File>()
        supportedLanguage.forEach {
            val file = getFile(it)
            if (file.exists() && file.length() > 0) {
                map[it] = file
            }
        }
        return map
    }
}