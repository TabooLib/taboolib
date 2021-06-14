package taboolib.platform

import org.bukkit.plugin.java.JavaPlugin
import taboolib.common.platform.PlatformAPI
import taboolib.common.platform.PlatformIO
import java.io.File

/**
 * TabooLib
 * taboolib.platform.BukkitIO
 *
 * @author sky
 * @since 2021/6/14 11:10 下午
 */
@PlatformAPI
class BukkitIO : PlatformIO {

    private val plugin = JavaPlugin.getProvidingPlugin(BukkitIO::class.java) as BukkitPlugin

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
        file.writeBytes(plugin.getResource(path)?.readBytes() ?: kotlin.error("resource not found: $path"))
        return file
    }

    override fun getJarFile(): File {
        return plugin.file
    }
}