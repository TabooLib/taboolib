package taboolib.common.platform.service

import taboolib.common.platform.PlatformService
import java.io.File

@PlatformService
interface PlatformIO {

    val pluginId: String

    val pluginVersion: String

    val isPrimaryThread: Boolean

    fun <T> server(): T

    fun info(vararg message: Any?)

    fun severe(vararg message: Any?)

    fun warning(vararg message: Any?)

    fun releaseResourceFile(source: String, target: String = source, replace: Boolean = false): File

    fun getJarFile(): File

    fun getDataFolder(): File

    fun getPlatformData(): Map<String, Any>
}