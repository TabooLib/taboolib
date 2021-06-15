package taboolib.platform

import taboolib.common.platform.PlatformInstance
import taboolib.common.platform.PlatformIO
import java.io.File
import java.util.logging.Logger

/**
 * TabooLib
 * taboolib.platform.BungeeIO
 *
 * @author sky
 * @since 2021/6/14 11:10 下午
 */
@PlatformInstance
class BungeeIO : PlatformIO {

    private val logger: Logger
        get() = try {
            BungeePlugin.instance.logger
        } catch (ex: Exception) {
            Logger.getAnonymousLogger()
        }

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
        val file = File(BungeePlugin.instance.dataFolder, path)
        if (file.exists() && !replace) {
            return file
        }
        file.writeBytes(BungeePlugin.instance.getResourceAsStream(path)?.readBytes() ?: kotlin.error("resource not found: $path"))
        return file
    }

    override fun getJarFile(): File {
        return BungeePlugin.instance.file
    }
}