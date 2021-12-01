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
import kotlin.collections.HashMap

/**
 * https://launchermeta.mojang.com/mc/game/version_manifest.json
 *
 * @author sky
 * @since 2020-04-04 19:51
 */
object I18n11700 : I18nBase() {

    val locales = arrayOf(
        arrayOf("zh_cn", "9db7ddaa4a2c4c87304b491ccf4952cc208b6b6d"),
        arrayOf("zh_tw", "7c3db1d86f29005d22ee5d482e805d2c374b9b5e"),
        arrayOf("en_gb", "183945d2bbcd839edf61c8f85439f77804c9fd38")
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
        var locale = cache[player?.locale ?: "zh_cn"]
        if (locale == null) {
            locale = cache["en_gb"]
        }
        if (locale == null) {
            return "[ERROR LOCALE]"
        }
        val element = locale[entity.getInternalName()]
        return if (element == null) entity.name else element.asString
    }

    override fun getName(player: Player?, itemStack: ItemStack): String {
        var locale = cache[player?.locale ?: "zh_cn"]
        if (locale == null) {
            locale = cache["en_gb"]
        }
        if (locale == null) {
            return "[ERROR LOCALE]"
        }
        val element = locale[itemStack.getInternalName()]
        return if (element == null) itemStack.type.name.lowercase(Locale.getDefault()).replace("_", "") else element.asString
    }

    override fun getName(player: Player?, enchantment: Enchantment): String {
        var locale = cache[player?.locale ?: "zh_cn"]
        if (locale == null) {
            locale = cache["en_gb"]
        }
        if (locale == null) {
            return "[ERROR LOCALE]"
        }
        val element = locale[enchantment.getInternalName()]
        return if (element == null) enchantment.name else element.asString
    }

    override fun getName(player: Player?, potionEffectType: PotionEffectType): String {
        var locale = cache[player?.locale ?: "zh_cn"]
        if (locale == null) {
            locale = cache["en_gb"]
        }
        if (locale == null) {
            return "[ERROR LOCALE]"
        }
        val element = locale[potionEffectType.getInternalName()]
        return if (element == null) potionEffectType.name else element.asString
    }
}