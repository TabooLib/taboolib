package taboolib.platform

import org.apache.commons.lang3.time.DateFormatUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import taboolib.common.Inject
import taboolib.common.env.RuntimeDependency
import taboolib.common.io.newFile
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.service.PlatformIO
import java.io.File

/**
 * TabooLib
 * taboolib.platform.AfyBrokerIO
 *
 * @author Ling556
 * @since 2024/5/09 23:51
 */
@Awake
@Inject
@PlatformSide(Platform.AFYBROKER)
@RuntimeDependency(
    value = "!org.apache.commons:commons-lang3:3.5",
    test = "!org.apache.commons.lang3.concurrent.BasicThreadFactory"
)
class AfyBrokerIO : PlatformIO {

    val date: String
        get() = DateFormatUtils.format(System.currentTimeMillis(), "HH:mm:ss")

    private val logger: Logger by lazy {
        LoggerFactory.getLogger(pluginId)

    }
    override val pluginId: String by lazy {
        AfyBrokerPlugin.getInstance().description.name
    }
    override val pluginVersion: String by lazy {
        AfyBrokerPlugin.getInstance().description.version
    }

    override val isPrimaryThread: Boolean
        get() = true

    @Suppress("UNCHECKED_CAST")
    override fun <T> server(): T {
        return AfyBrokerPlugin.getInstance().server as T
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

    override fun releaseResourceFile(source: String, target: String, replace: Boolean): File {
        val file = File(getDataFolder(), target)
        if (file.exists() && !replace) {
            return file
        }
        newFile(file).writeBytes(
            javaClass.classLoader.getResourceAsStream(source)?.readBytes()
                ?: error("resource not found: $source")
        )
        return file
    }

    override fun getJarFile(): File {
        return AfyBrokerPlugin.getPluginInstance()?.nativeJarFile() ?: AfyBrokerPlugin.getInstance().file
    }

    override fun getDataFolder(): File {
        return AfyBrokerPlugin.getPluginInstance()?.nativeDataFolder() ?: AfyBrokerPlugin.getInstance().dataFolder
    }

    override fun getPlatformData(): Map<String, Any> {
        return emptyMap()
    }
}