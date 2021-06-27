package taboolib.platform

import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformIO
import taboolib.common.platform.PlatformSide
import java.io.File
import java.util.logging.Logger

/**
 * TabooLib
 * taboolib.platform.BungeeIO
 *
 * @author sky
 * @since 2021/6/14 11:10 下午
 */
@Awake
@PlatformSide([Platform.BUNGEE])
class BungeeIO : PlatformIO {

    private val logger: Logger
        get() = try {
            BungeePlugin.getInstance().logger
        } catch (ex: Exception) {
            Logger.getAnonymousLogger()
        }

    override val pluginId: String
        get() = BungeePlugin.getInstance().description.name

    override val isPrimaryThread: Boolean
        get() = true

    override fun info(vararg message: Any?) {
        message.filterNotNull().forEach { logger.info(it.toString()) }
    }

    override fun severe(vararg message: Any?) {
        message.filterNotNull().forEach { logger.severe(it.toString()) }
    }

    override fun warning(vararg message: Any?) {
        message.filterNotNull().forEach { logger.warning(it.toString()) }
    }

    override fun releaseResourceFile(path: String, replace: Boolean): File {
        val file = File(BungeePlugin.getInstance().dataFolder, path)
        if (file.exists() && !replace) {
            return file
        }
        if (!file.parentFile.exists()) {
            file.parentFile.mkdirs()
        }
        file.createNewFile()
        file.writeBytes(BungeePlugin.getInstance().getResourceAsStream(path)?.readBytes() ?: error("resource not found: $path"))
        return file
    }

    override fun getJarFile(): File {
        return BungeePlugin.getInstance().file
    }

    override fun getDataFolder(): File {
        return BungeePlugin.getInstance().dataFolder
    }
}