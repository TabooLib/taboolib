package taboolib.platform

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.spongepowered.api.Sponge
import taboolib.common.io.newFile
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.service.PlatformIO
import java.io.File

/**
 * TabooLib
 * taboolib.platform.SpongeIO
 *
 * @author sky
 * @since 2021/6/14 11:10 下午
 */
@Awake
@PlatformSide([Platform.SPONGE_API_8])
class Sponge8IO : PlatformIO {

    private val logger: Logger
        get() = try {
            Sponge8Plugin.getInstance().pluginContainer.logger()
        } catch (ex: Exception) {
            LogManager.getLogger("Anonymous")
        }

    override val pluginId: String
        get() = Sponge8Plugin.getInstance().pluginContainer.metadata().id()

    override val pluginVersion: String
        get() = Sponge8Plugin.getInstance().pluginContainer.metadata().version().qualifier

    override val isPrimaryThread: Boolean
        get() = Sponge.server().onMainThread()

    @Suppress("UNCHECKED_CAST")
    override fun <T> server(): T {
        return Sponge.server() as T
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

    override fun releaseResourceFile(source: String, target: String = source, replace: Boolean): File {
        val file = File(getDataFolder(), target)
        if (file.exists() && !replace) {
            return file
        }
        newFile(file).writeBytes(javaClass.classLoader.getResourceAsStream(source)?.readBytes() ?: error("resource not found: $source"))
        return file
    }

    override fun getJarFile(): File {
        return Sponge8Plugin.getPluginInstance()?.nativeJarFile() ?: File(Sponge8Plugin::class.java.protectionDomain.codeSource.location.toURI().path)
    }

    override fun getDataFolder(): File {
        return Sponge8Plugin.getPluginInstance()?.nativeDataFolder() ?: Sponge8Plugin.getInstance().pluginConfigDir
    }

    override fun getPlatformData(): Map<String, Any> {
        val platform = Sponge.game().platform()
        return mapOf(
            "minecraftVersion" to platform.minecraftVersion().name(),
            "onlineMode" to if (Sponge.server().isOnlineModeEnabled) 1 else 0,
            "spongeImplementation" to platform.container(org.spongepowered.api.Platform.Component.IMPLEMENTATION).metadata().id(),
        )
    }
}