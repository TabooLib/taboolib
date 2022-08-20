package taboolib.platform

import org.apache.commons.lang3.time.DateFormatUtils
import taboolib.common.env.RuntimeDependency
import taboolib.common.io.newFile
import taboolib.common.platform.Awake
import taboolib.common.platform.Platform
import taboolib.common.platform.PlatformSide
import taboolib.common.platform.service.PlatformIO
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
@RuntimeDependency(value = "!org.apache.commons:commons-lang3:3.5", test = "!org.apache.commons.lang3.concurrent.BasicThreadFactory")
class AppIO : PlatformIO {

    val date: String
        get() = DateFormatUtils.format(System.currentTimeMillis(), "HH:mm:ss")

    override val pluginId: String
        get() = "application"

    override val pluginVersion: String
        get() = "application"

    override val isPrimaryThread: Boolean
        get() = true

    override fun <T> server(): T {
        TODO("Not yet implemented")
    }

    override fun info(vararg message: Any?) {
        message.filterNotNull().forEach { println("[${date}][INFO] $it") }
    }

    override fun severe(vararg message: Any?) {
        message.filterNotNull().forEach { println("[${date}][ERROR] $it") }
    }

    override fun warning(vararg message: Any?) {
        message.filterNotNull().forEach { println("[${date}][WARN] $it") }
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
        return File(AppIO::class.java.protectionDomain.codeSource.location.toURI().path)
    }

    override fun getDataFolder(): File {
        return nativeDataFolder ?: File(getJarFile().parent)
    }

    override fun getPlatformData(): Map<String, Any> {
        return emptyMap()
    }

    companion object {

        var nativeDataFolder: File? = null
    }
}