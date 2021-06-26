package taboolib.common.platform

import java.io.File

interface PlatformIO {

    val pluginId: String

    val isPrimaryThread: Boolean

    val runningPlatform: Platform

    fun info(vararg message: Any?)

    fun severe(vararg message: Any?)

    fun warning(vararg message: Any?)

    fun releaseResourceFile(path: String, replace: Boolean = false): File

    fun getJarFile(): File

    fun getDataFolder(): File
}