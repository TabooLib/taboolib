package taboolib.module.nms.i18n

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffectType
import taboolib.module.nms.getInternalName
import java.io.File
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.concurrent.Executors

/**
 * https://launchermeta.mojang.com/mc/game/version_manifest.json -> [version] -> assetIndex ->
 *
 * @author sky
 * @since 2020-04-04 19:51
 */
object I18n11700 : I18nBase() {

    val locales = arrayOf(
        arrayOf("zh_cn", "9fabbc798786c7b7364902b31bdb37c2c037e9b8"),
        arrayOf("zh_tw", "7b5f43b3b856f1c3ebca3bd207c2af5d39ce9da1"),
        arrayOf("en_gb", "85b316bb1dc13c9e09d00641b61cea71b6b42fa9")
    )

    private val cache: MutableMap<String, JsonObject> = HashMap()
    private val folder = File("assets")
    private val executor = Executors.newSingleThreadExecutor()

    fun load() {
        locales.forEach {
            val file = File(folder, "${it[1].substring(0, 2)}/${it[1]}")
            if (file.exists()) {
                cache[it[0]] = JsonParser().parse(file.readText(StandardCharsets.UTF_8)).asJsonObject
            }
        }
    }

    override fun init() {
        executor.submit {
            load()
            if (cache.isEmpty()) {
                println("Loading language files, please wait...")
                try {
                    locales.forEach {
                        val file = File(folder, "${it[1].substring(0, 2)}/${it[1]}")
                        if (!file.parentFile.exists()) {
                            file.parentFile.mkdirs()
                        }
                        file.createNewFile()
                        file.writeBytes(URL("https://resources.download.minecraft.net/" + it[1].substring(0, 2) + "/" + it[1]).openStream().readBytes())
                    }
                    load()
                } catch (ex: Throwable) {
                    ex.printStackTrace()
                }
            }
        }
    }

    override fun getName(player: Player?, entity: Entity): String {
        val locale = getLocale(player) ?: return "[ERROR LOCALE]"
        val element = locale[entity.getInternalName()]
        return if (element == null) entity.name else element.asString
    }

    override fun getName(player: Player?, itemStack: ItemStack): String {
        val locale = getLocale(player) ?: return "[ERROR LOCALE]"
        val element = locale[itemStack.getInternalName()]
        return if (element == null) itemStack.type.name.lowercase(Locale.getDefault()).replace("_", "") else element.asString
    }

    override fun getName(player: Player?, enchantment: Enchantment): String {
        val locale = getLocale(player) ?: return "[ERROR LOCALE]"
        val element = locale[enchantment.getInternalName()]
        return if (element == null) enchantment.name else element.asString
    }

    override fun getName(player: Player?, potionEffectType: PotionEffectType): String {
        val locale = getLocale(player) ?: return "[ERROR LOCALE]"
        val element = locale[potionEffectType.getInternalName()]
        return if (element == null) potionEffectType.name else element.asString
    }

    private fun getLocale(player: Player?): JsonObject? {
        var locale = cache[player?.locale ?: "zh_cn"]
        if (locale == null) {
            locale = cache["en_gb"]
        }
        if (locale == null) {
            return null
        }
        return locale
    }
}