package taboolib.platform

import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import taboolib.common.platform.Platform
import taboolib.common.platform.Awake
import taboolib.common.platform.PlatformIO
import taboolib.common.platform.PlatformSide
import java.io.File

/**
 * TabooLib
 * taboolib.platform.BukkitIO
 *
 * @author sky
 * @since 2021/6/14 11:10 下午
 */
@Awake
@PlatformSide([Platform.BUKKIT])
class BukkitIO : PlatformIO {

    val plugin: BukkitPlugin
        get() = BukkitPlugin.getInstance()

    override val pluginId: String
        get() = plugin.description.name

    override val isPrimaryThread: Boolean
        get() = Bukkit.isPrimaryThread()

    override fun info(vararg message: Any?) {
        message.filterNotNull().forEach { plugin.logger.info(it.toString()) }
    }

    override fun severe(vararg message: Any?) {
        message.filterNotNull().forEach { plugin.logger.severe(it.toString()) }
    }

    override fun warning(vararg message: Any?) {
        message.filterNotNull().forEach { plugin.logger.warning(it.toString()) }
    }

    override fun releaseResourceFile(path: String, replace: Boolean): File {
        val file = File(plugin.dataFolder, path)
        if (file.exists() && !replace) {
            return file
        }
        if (!file.parentFile.exists()) {
            file.parentFile.mkdirs()
        }
        file.createNewFile()
        file.writeBytes(plugin.getResource(path)?.readBytes() ?: error("resource not found: $path"))
        return file
    }

    override fun getJarFile(): File {
        return plugin.file
    }

    override fun getDataFolder(): File {
        return plugin.dataFolder
    }
}