package taboolib.common.platform

import taboolib.common.OpenContainer
import java.io.File

interface PlatformIO {

    val pluginId: String

    val isPrimaryThread: Boolean

    fun info(vararg message: Any?)

    fun severe(vararg message: Any?)

    fun warning(vararg message: Any?)

    fun releaseResourceFile(path: String, replace: Boolean = false): File

    fun getJarFile(): File

    fun getDataFolder(): File

    fun getOpenContainers(): List<OpenContainer>
}