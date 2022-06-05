package taboolib.platform

import org.slf4j.Logger
import org.slf4j.LoggerFactory
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
@PlatformSide([Platform.SPONGE_API_7])
class Sponge7IO : PlatformIO {

    private val logger: Logger
        get() = try {
            Sponge7Plugin.getInstance().pluginContainer.logger
        } catch (ex: Exception) {
            LoggerFactory.getLogger("Anonymous")
        }

    override val pluginId: String
        get() = Sponge7Plugin.getInstance().pluginContainer.id

    override val pluginVersion: String
        get() = Sponge7Plugin.getInstance().pluginContainer.version.orElse("null")

    override val isPrimaryThread: Boolean
        get() = Sponge.getServer().isMainThread

    @Suppress("UNCHECKED_CAST")
    override fun <T> server(): T {
        return Sponge.getServer() as T
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
        newFile(file).writeBytes(javaClass.classLoader.getResourceAsStream(path)?.readBytes() ?: error("resource not found: $path"))
        return file
    }

    override fun getJarFile(): File {
        return Sponge7Plugin.getPluginInstance()?.nativeJarFile() ?: File(Sponge7Plugin.getInstance().pluginContainer.source.get().toUri().path)
    }

    override fun getDataFolder(): File {
        return Sponge7Plugin.getPluginInstance()?.nativeDataFolder() ?: Sponge7Plugin.getInstance().pluginConfigDir
    }

    override fun getPlatformData(): Map<String, Any> {
        val platform = Sponge.getGame().platform
        return mapOf(
            "minecraftVersion" to platform.minecraftVersion.name,
            "onlineMode" to if (Sponge.getServer().onlineMode) 1 else 0,
            "spongeImplementation" to platform.getContainer(org.spongepowered.api.Platform.Component.IMPLEMENTATION).name,
        )
    }
}