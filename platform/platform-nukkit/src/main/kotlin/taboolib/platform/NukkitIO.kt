package taboolib.platform

import cn.nukkit.Server
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import taboolib.common.io.newFile
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.service.PlatformIO
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

    override val pluginVersion: String
        get() = NukkitPlugin.getInstance().description.version

    override val isPrimaryThread: Boolean
        get() = NukkitPlugin.getInstance().server.isPrimaryThread

    @Suppress("UNCHECKED_CAST")
    override fun <T> server(): T {
        return Server.getInstance() as T
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
        val file = File(getDataFolder(), path)
        if (file.exists() && !replace) {
            return file
        }
        newFile(file).writeBytes(NukkitPlugin.getInstance().getResource(path)?.readBytes() ?: error("resource not found: $path"))
        return file
    }

    override fun getJarFile(): File {
        return NukkitPlugin.getPluginInstance()?.nativeJarFile() ?: NukkitPlugin.getInstance().file
    }

    override fun getDataFolder(): File {
        return NukkitPlugin.getPluginInstance()?.nativeDataFolder() ?: NukkitPlugin.getInstance().dataFolder
    }

    override fun getPlatformData(): Map<String, Any> {
        return mapOf(
            "nukkitVersion" to Server.getInstance().version,
            "nukkitName" to Server.getInstance().name,
            "onlineMode" to 0
        )
    }
}