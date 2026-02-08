package taboolib.platform.bukkit

import org.bukkit.Bukkit
import org.bukkit.Server
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.generator.BiomeProvider
import org.bukkit.generator.ChunkGenerator
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.PluginDescriptionFile
import org.bukkit.plugin.PluginLoader
import org.bukkit.plugin.ServicePriority
import java.io.File
import java.io.InputStream
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Logger

/**
 * 借助 BukkitServicesManager 注册一个全局的 ConcurrentHashMap 用于存储数据
 * 用于解决不同插件之间的数据交换问题，例如 A 加载的映射文件，B 插件无需重复加载
 */
@Suppress("UNCHECKED_CAST")
object Exchanges {

    // Minecraft 语言文件缓存
    const val MINECRAFT_LANGUAGE = "minecraft_language"

    // Spigot 映射表
    const val MAPPING_SPIGOT = "mapping_spigot"

    // Paper 映射表
    const val MAPPING_PAPER = "mapping_paper"

    private val map: MutableMap<String, Any>

    init {
        val registration = Bukkit.getServicesManager().getRegistrations(ConcurrentHashMap::class.java).find { it.plugin.name == ExchangePlugin.name }
        if (registration != null) {
            map = registration.provider as ConcurrentHashMap<String, Any>
        } else {
            map = ConcurrentHashMap()
            Bukkit.getServicesManager().register(ConcurrentHashMap::class.java, map, ExchangePlugin, ServicePriority.Low)
        }
    }

    /**
     * 是否存在数据
     */
    operator fun contains(key: String): Boolean {
        return map.containsKey(key)
    }

    /**
     * 读取数据
     */
    operator fun <T> get(key: String): T {
        return map[key] as T
    }

    /**
     * 读取数据，如果不存在则写入默认值
     */
    fun <T> getOrPut(key: String, defaultValue: () -> T): T {
        return map.getOrPut(key) { defaultValue()!! } as T
    }

    /**
     * 写入数据
     */
    operator fun set(key: String, value: Any?) {
        if (value != null) {
            map[key] = value
        } else {
            map.remove(key)
        }
    }

    /**
     * 获取容器
     */
    operator fun invoke(): MutableMap<String, Any> {
        return map
    }

    // region ExchangePlugin
    object ExchangePlugin : Plugin {

        override fun onTabComplete(p0: CommandSender, p1: Command, p2: String, p3: Array<out String>?): MutableList<String>? {
            error("Unsupported operation")
        }

        override fun onCommand(p0: CommandSender, p1: Command, p2: String, p3: Array<out String>?): Boolean {
            error("Unsupported operation")
        }

        override fun getDataFolder(): File {
            error("Unsupported operation")
        }

        override fun getDescription(): PluginDescriptionFile {
            error("Unsupported operation")
        }

        override fun getConfig(): FileConfiguration {
            error("Unsupported operation")
        }

        override fun getResource(p0: String): InputStream? {
            error("Unsupported operation")
        }

        override fun saveConfig() {
        }

        override fun saveDefaultConfig() {
        }

        override fun saveResource(p0: String, p1: Boolean) {
        }

        override fun reloadConfig() {
        }

        override fun getPluginLoader(): PluginLoader {
            error("Unsupported operation")
        }

        override fun getServer(): Server {
            error("Unsupported operation")
        }

        override fun isEnabled(): Boolean {
            error("Unsupported operation")
        }

        override fun onDisable() {
        }

        override fun onLoad() {
        }

        override fun onEnable() {
        }

        override fun isNaggable(): Boolean {
            error("Unsupported operation")
        }

        override fun setNaggable(p0: Boolean) {
        }

        override fun getDefaultWorldGenerator(p0: String, p1: String?): ChunkGenerator? {
            error("Unsupported operation")
        }

        override fun getDefaultBiomeProvider(p0: String, p1: String?): BiomeProvider? {
            error("Unsupported operation")
        }

        override fun getLogger(): Logger {
            error("Unsupported operation")
        }

        override fun getName(): String {
            return "TabooLibExchange/v1"
        }
    }
    // endregion
}