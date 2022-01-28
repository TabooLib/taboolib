package taboolib.common.platform.function

import taboolib.common.platform.PlatformFactory
import taboolib.common.platform.service.PlatformIO
import java.io.File

val pluginId: String
    get() = PlatformFactory.getPlatformService<PlatformIO>().pluginId

val pluginVersion: String
    get() = PlatformFactory.getPlatformService<PlatformIO>().pluginVersion

val isPrimaryThread: Boolean
    get() = PlatformFactory.getPlatformService<PlatformIO>().isPrimaryThread

fun <T> server(): T {
    return PlatformFactory.getPlatformService<PlatformIO>().server()
}

fun info(vararg message: Any?) {
    PlatformFactory.getPlatformService<PlatformIO>().info(*message)
}

fun severe(vararg message: Any?) {
    PlatformFactory.getPlatformService<PlatformIO>().severe(*message)
}

fun warning(vararg message: Any?) {
    PlatformFactory.getPlatformService<PlatformIO>().warning(*message)
}

fun releaseResourceFile(path: String, replace: Boolean = false): File {
    return PlatformFactory.getPlatformService<PlatformIO>().releaseResourceFile(path, replace)
}

fun getJarFile(): File {
    return PlatformFactory.getPlatformService<PlatformIO>().getJarFile()
}

fun getDataFolder(): File {
    return PlatformFactory.getPlatformService<PlatformIO>().getDataFolder()
}

fun getPlatformData(): Map<String, Any> {
    return PlatformFactory.getPlatformService<PlatformIO>().getPlatformData()
}