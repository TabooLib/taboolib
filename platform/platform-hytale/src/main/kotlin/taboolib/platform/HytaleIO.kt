package taboolib.platform

import com.hypixel.hytale.server.core.HytaleServer
import taboolib.common.Inject
import taboolib.common.io.newFile
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.service.PlatformIO
import taboolib.common.util.unsafeLazy
import java.io.File
import java.util.logging.Level

/**
 * TabooLib
 * taboolib.platform.HytaleIO
 *
 * @author sky
 * @since 2024/1/1
 */
@Awake
@Inject
@PlatformSide(Platform.HYTALE)
class HytaleIO : PlatformIO {

    val plugin by unsafeLazy { HytalePlugin.getInstance() }

    override val pluginId: String
        get() = plugin.manifest.name

    override val pluginVersion: String
        get() = plugin.manifest.version.toString()

    override val isPrimaryThread: Boolean
        get() = true // Hytale 没有主线程概念，所有任务都是异步的

    @Suppress("UNCHECKED_CAST")
    override fun <T> server(): T {
        return HytaleServer.get() as T
    }

    override fun info(vararg message: Any?) {
        message.filterNotNull().forEach { plugin.logger.at(Level.INFO).log(it.toString()) }
    }

    override fun severe(vararg message: Any?) {
        message.filterNotNull().forEach { plugin.logger.at(Level.SEVERE).log(it.toString()) }
    }

    override fun warning(vararg message: Any?) {
        message.filterNotNull().forEach { plugin.logger.at(Level.WARNING).log(it.toString()) }
    }

    override fun releaseResourceFile(source: String, target: String, replace: Boolean): File {
        val file = File(getDataFolder(), target)
        if (file.exists() && !replace) {
            return file
        }
        newFile(file).writeBytes(javaClass.classLoader.getResourceAsStream(source)?.readBytes() ?: error("resource not found: $source"))
        return file
    }

    override fun getJarFile(): File {
        return HytalePlugin.getPluginInstance()?.nativeJarFile() ?: plugin.pluginFile
    }

    override fun getDataFolder(): File {
        return HytalePlugin.getPluginInstance()?.nativeDataFolder() ?: plugin.pluginDataFolder
    }

    override fun getPlatformData(): Map<String, Any> {
        return mapOf(
            "serverVersion" to HytaleServer.get().config.serverName,
            "pluginName" to plugin.manifest.name,
            "pluginVersion" to plugin.manifest.version.toString()
        )
    }
}
