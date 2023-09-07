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

    val isLog4jEnabled by lazy {
        try {
            Class.forName("org.apache.log4j.Logger")
            true
        } catch (e: ClassNotFoundException) {
            false
        }
    }

    override var pluginId = "application"

    override var pluginVersion = "application"

    override val isPrimaryThread: Boolean
        get() = true

    override fun <T> server(): T {
        TODO("Not yet implemented")
    }

    override fun info(vararg message: Any?) {
        message.filterNotNull().forEach {
            if (isLog4jEnabled) {
                println(it)
            } else {
                println("[${date}][INFO] $it")
            }
        }
    }

    override fun severe(vararg message: Any?) {
        message.filterNotNull().forEach {
            if (isLog4jEnabled) {
                println(it)
            } else {
                println("[${date}][ERROR] $it")
            }
        }
    }

    override fun warning(vararg message: Any?) {
        message.filterNotNull().forEach {
            if (isLog4jEnabled) {
                println(it)
            } else {
                println("[${date}][WARN] $it")
            }
        }
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