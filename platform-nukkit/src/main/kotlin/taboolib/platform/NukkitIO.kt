package taboolib.platform

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import taboolib.common.platform.PlatformAPI
import taboolib.common.platform.PlatformIO
import java.io.File

/**
 * TabooLib
 * taboolib.platform.NukkitIO
 *
 * @author sky
 * @since 2021/6/14 11:10 下午
 */
@PlatformAPI
class NukkitIO : PlatformIO {

    private val logger: Logger
        get() = try {
            NukkitPlugin.instance.logger
        } catch (ex: Exception) {
            LoggerFactory.getLogger("Anonymous")
        }

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
        val file = File(NukkitPlugin.instance.dataFolder, path)
        if (file.exists() && !replace) {
            return file
        }
        file.writeBytes(NukkitPlugin.instance.getResource(path)?.readBytes() ?: kotlin.error("resource not found: $path"))
        return file
    }

    override fun getJarFile(): File {
        return NukkitPlugin.instance.file
    }
}