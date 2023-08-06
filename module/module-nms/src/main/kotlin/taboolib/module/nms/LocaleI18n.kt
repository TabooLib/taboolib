package taboolib.module.nms

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import taboolib.common.LifeCycle
import taboolib.common.TabooLibCommon
import taboolib.common.io.digest
import taboolib.common.io.newFile
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import java.io.File
import java.net.URL

/**
 * TabooLib
 * taboolib.module.nms.LocaleI18n
 *
 * @author 坏黑
 * @since 2023/8/6 02:44
 */
@PlatformSide([Platform.BUKKIT])
object LocaleI18n {

    /** 资源文件地址 */
    var resourceUrl = "https://resources.download.minecraft.net"

    /** 支持的语言文件 */
    val supportedLanguage = arrayListOf("zh_cn", "zh_hk", "zh_tw", "en_gb")

    /**
     * 获取语言文件（可能不存在）
     */
    fun getLocalFile(locale: String): File {
        val hash = (MinecraftVersion.runningVersion + locale).digest("sha-1")
        return File("assets/${hash.substring(0, 2)}/$hash")
    }

    /**
     * 获取语言文件
     * 仅可获取到有效的语言文件
     */
    fun getLocalFiles(): Map<String, File> {
        val map = HashMap<String, File>()
        supportedLanguage.forEach {
            val file = getLocalFile(it)
            if (file.exists() && file.length() > 0) {
                map[it] = file
            }
        }
        return map
    }

    @Awake(LifeCycle.INIT)
    private fun init() {
        if (checkLocaleFile()) {
            return
        }
        downloadLocaleFile()
    }

    /** 检查语言文件 */
    private fun checkLocaleFile(): Boolean {
        return getLocalFiles().size != supportedLanguage.size
    }

    /** 下载语言文件 */
    private fun downloadLocaleFile() {
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
                            val file = getLocalFile(language)
                            if (file.exists() && file.length() > 0) {
                                break
                            }
                            TabooLibCommon.print("Downloading language ... $language")
                            // 获取语言文件文本并写入本地文件
                            newFile(file).writeText(URL("$resourceUrl/${langHash.substring(0, 2)}/$langHash").readText())
                            break
                        }
                    }
                }
                break
            }
        }
    }

    private fun readJson(url: String): JsonObject {
        return JsonParser().parse(URL(url).readText()).asJsonObject
    }
}