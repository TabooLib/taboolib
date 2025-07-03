package taboolib.platform

import taboolib.common.Inject
import taboolib.common.io.newFile
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.service.PlatformIO
import taboolib.common.util.unsafeLazy
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
@Inject
@PlatformSide(Platform.BUNGEE)
class BungeeIO : PlatformIO {

    val plugin by unsafeLazy { BungeePlugin.getInstance() }

    private val logger: Logger
        get() = try {
            BungeePlugin.getInstance().logger
        } catch (ex: Exception) {
            Logger.getAnonymousLogger()
        }

    override val pluginId: String
        get() = BungeePlugin.getInstance().description.name

    override val pluginVersion: String
        get() = BungeePlugin.getInstance().description.version

    override val isPrimaryThread: Boolean
        get() = true

    @Suppress("UNCHECKED_CAST")
    override fun <T> server(): T {
        return plugin.proxy as T
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

    override fun releaseResourceFile(source: String, target: String, replace: Boolean): File {
        val file = File(getDataFolder(), target)
        if (file.exists() && !replace) {
            return file
        }
        newFile(file).writeBytes(javaClass.classLoader.getResourceAsStream(source)?.readBytes() ?: error("resource not found: $source"))
        return file
    }

    override fun getJarFile(): File {
        return BungeePlugin.getPluginInstance()?.nativeJarFile() ?: BungeePlugin.getInstance().file
    }

    override fun getDataFolder(): File {
        return BungeePlugin.getPluginInstance()?.nativeDataFolder() ?: BungeePlugin.getInstance().dataFolder
    }

    override fun getPlatformData(): Map<String, Any> {
        val proxy = BungeePlugin.getInstance().proxy
        return mapOf(
            "managedServers" to proxy.servers.size,
            "onlineMode" to if (proxy.config.isOnlineMode) 1 else 0,
            "bungeecordVersion" to proxy.version,
        )
    }
}