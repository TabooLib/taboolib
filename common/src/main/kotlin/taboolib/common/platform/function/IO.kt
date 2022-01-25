package taboolib.common.platform.function

import taboolib.common.platform.PlatformFactory
import taboolib.common.platform.service.PlatformIO
import java.io.File

val pluginId: String
    get() = PlatformFactory.getService<PlatformIO>().pluginId

val pluginVersion: String
    get() = PlatformFactory.getService<PlatformIO>().pluginVersion

val isPrimaryThread: Boolean
    get() = PlatformFactory.getService<PlatformIO>().isPrimaryThread

fun <T> server(): T {
    return PlatformFactory.getService<PlatformIO>().server()
}

fun info(vararg message: Any?) {
    PlatformFactory.getService<PlatformIO>().info(*message)
}

fun severe(vararg message: Any?) {
    PlatformFactory.getService<PlatformIO>().severe(*message)
}

fun warning(vararg message: Any?) {
    PlatformFactory.getService<PlatformIO>().warning(*message)
}

fun releaseResourceFile(path: String, replace: Boolean = false): File {
    return PlatformFactory.getService<PlatformIO>().releaseResourceFile(path, replace)
}

fun getJarFile(): File {
    return PlatformFactory.getService<PlatformIO>().getJarFile()
}

fun getDataFolder(): File {
    return PlatformFactory.getService<PlatformIO>().getDataFolder()
}

fun getPlatformData(): Map<String, Any> {
    return PlatformFactory.getService<PlatformIO>().getPlatformData()
}