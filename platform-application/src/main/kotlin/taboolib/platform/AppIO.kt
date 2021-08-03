package taboolib.platform

import taboolib.common.OpenContainer
import taboolib.common.io.newFile
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformIO
import taboolib.common.platform.PlatformSide
import java.io.File

/**
 * TabooLib
 * taboolib.platform.AppIO
 *
 * @author sky
 * @since 2021/6/14 11:10 下午
 */
@Awake
@PlatformSide([Platform.APPLICATION])
class AppIO : PlatformIO {

    override val pluginId: String
        get() = "application"

    override val isPrimaryThread: Boolean
        get() = true

    override fun info(vararg message: Any?) {
        message.filterNotNull().forEach { println("[info] $it") }
    }

    override fun severe(vararg message: Any?) {
        message.filterNotNull().forEach { println("[error] $it") }
    }

    override fun warning(vararg message: Any?) {
        message.filterNotNull().forEach { println("[warning] $it") }
    }

    override fun releaseResourceFile(path: String, replace: Boolean): File {
        val file = File(getDataFolder(), path)
        if (file.exists() && !replace) {
            return file
        }
        newFile(file, create = true).writeBytes(javaClass.classLoader.getResourceAsStream(path)?.readBytes() ?: error("resource not found: $path"))
        return file
    }

    override fun getJarFile(): File {
        return File(AppIO::class.java.protectionDomain.codeSource.location.toURI().path)
    }

    override fun getDataFolder(): File {
        return File(getJarFile().parentFile.parentFile, pluginId)
    }

    override fun getPlatformData(): Map<String, Any> {
        return emptyMap()
    }

    override fun getOpenContainers(): List<OpenContainer> {
        return emptyList()
    }
}