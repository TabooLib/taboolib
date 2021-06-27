package taboolib.platform

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformIO
import taboolib.common.platform.PlatformSide
import java.io.File

/**
 * TabooLib
 * taboolib.platform.NukkitIO
 *
 * @author sky
 * @since 2021/6/14 11:10 下午
 */
@Awake
@PlatformSide([Platform.NUKKIT])
class NukkitIO : PlatformIO {

    private val logger: Logger
        get() = try {
            NukkitPlugin.getInstance().logger
        } catch (ex: Exception) {
            LoggerFactory.getLogger("Anonymous")
        }

    override val pluginId: String
        get() = NukkitPlugin.getInstance().name

    override val isPrimaryThread: Boolean
        get() = NukkitPlugin.getInstance().server.isPrimaryThread

    override fun info(vararg message: Any?) {
        message.filterNotNull().forEach { logger.info(it.toString()) }
    }

    override fun severe(vararg message: Any?) {
        message.filterNotNull().forEach { logger.error(it.toString()) }
    }

    override fun warning(vararg message: Any?) {
        message.filterNotNull().forEach { logger.warn(it.toString()) }
    }

    override fun releaseResourceFile(path: String, replace: Boolean): File {
        val file = File(NukkitPlugin.getInstance().dataFolder, path)
        if (file.exists() && !replace) {
            return file
        }
        if (!file.parentFile.exists()) {
            file.parentFile.mkdirs()
        }
        file.createNewFile()
        file.writeBytes(NukkitPlugin.getInstance().getResource(path)?.readBytes() ?: kotlin.error("resource not found: $path"))
        return file
    }

    override fun getJarFile(): File {
        return NukkitPlugin.getInstance().file
    }

    override fun getDataFolder(): File {
        return NukkitPlugin.getInstance().dataFolder
    }
}