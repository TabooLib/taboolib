package taboolib.platform

import de.dytanic.cloudnet.CloudNet
import de.dytanic.cloudnet.common.logging.DefaultAsyncLogger
import de.dytanic.cloudnet.common.logging.ILogger
import taboolib.common.io.newFile
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.service.PlatformIO
import java.io.File

/**
 * TabooLib
 * taboolib.platform.BungeeIO
 *
 * @author sky
 * @since 2021/6/14 11:10 下午
 */
@Awake
@PlatformSide([Platform.CLOUDNET_V3])
class CloudNetV3IO : PlatformIO {

    val plugin by unsafeLazy { CloudNetV3Plugin.getInstance() }

    private val logger: ILogger
        get() = try {
            CloudNetV3Plugin.getInstance().logger
        } catch (ex: Exception) {
            DefaultAsyncLogger()
        }

    override val pluginId: String
        get() = CloudNetV3Plugin.getInstance().name

    override val pluginVersion: String
        get() = CloudNetV3Plugin.getInstance().version

    override val isPrimaryThread: Boolean
        get() = true

    @Suppress("UNCHECKED_CAST")
    override fun <T> server(): T {
        return CloudNet.getInstance() as T
    }

    override fun info(vararg message: Any?) {
        message.filterNotNull().forEach { logger.info(it.toString()) }
    }

    override fun severe(vararg message: Any?) {
        message.filterNotNull().forEach { logger.error(it.toString()) }
    }

    override fun warning(vararg message: Any?) {
        message.filterNotNull().forEach { logger.warning(it.toString()) }
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
        return CloudNetV3Plugin.getPluginInstance()?.nativeJarFile() ?: File(CloudNetV3Plugin::class.java.protectionDomain.codeSource.location.toURI().path)
    }

    override fun getDataFolder(): File {
        return CloudNetV3Plugin.getPluginInstance()?.nativeDataFolder() ?: CloudNetV3Plugin.getInstance().moduleWrapper.dataDirectory.toFile()
    }

    override fun getPlatformData(): Map<String, Any> {
        val proxy = CloudNet.getInstance()
        return mapOf(
            "managedServers" to proxy.cloudServiceManager.cloudServices.size
        )
    }
}