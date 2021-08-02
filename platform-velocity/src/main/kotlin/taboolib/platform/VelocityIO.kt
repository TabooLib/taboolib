package taboolib.platform

import com.velocitypowered.api.plugin.Plugin
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import taboolib.common.OpenContainer
import taboolib.common.io.newFile
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformIO
import taboolib.common.platform.PlatformSide
import taboolib.platform.type.VelocityOpenContainer
import java.io.File

/**
 * TabooLib
 * taboolib.platform.VelocityIO
 *
 * @author sky
 * @since 2021/6/14 11:10 下午
 */
@Awake
@PlatformSide([Platform.VELOCITY])
class VelocityIO : PlatformIO {

    private val logger: Logger
        get() = try {
            VelocityPlugin.getInstance().logger
        } catch (ex: Exception) {
            LoggerFactory.getLogger("Anonymous")
        }

    override val pluginId: String
        get() = VelocityPlugin::class.java.getAnnotation(Plugin::class.java).id

    override val isPrimaryThread: Boolean
        get() = true

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
        val file = File(VelocityPlugin.getInstance().configDirectory.toFile(), path)
        if (file.exists() && !replace) {
            return file
        }
        newFile(file).writeBytes(javaClass.classLoader.getResourceAsStream(path)?.readBytes() ?: error("resource not found: $path"))
        return file
    }

    override fun getJarFile(): File {
        return File(VelocityPlugin::class.java.protectionDomain.codeSource.location.toURI().path)
    }

    override fun getDataFolder(): File {
        return VelocityPlugin.getInstance().configDirectory.toFile()
    }

    override fun getPlatformData(): Map<String, Any> {
        val server = VelocityPlugin.getInstance().server
        return mapOf(
            "managedServers" to server.allServers.size,
            "onlineMode" to if (server.configuration.isOnlineMode) 1 else 0,
            "velocityVersionVersion" to server.version.version,
            "velocityVersionName" to server.version.name,
            "velocityVersionVendor" to server.version.vendor,
        )
    }

    override fun getOpenContainers(): List<OpenContainer> {
        return VelocityPlugin.getInstance().server.pluginManager.plugins
            .filter { it.instance.orElse(null)?.javaClass?.name?.endsWith("taboolib.platform.VelocityPlugin") == true }
            .mapNotNull {
                try {
                    VelocityOpenContainer(it)
                } catch (ex: Throwable) {
                    ex.printStackTrace()
                    null
                }
            }
    }
}