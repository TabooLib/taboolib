package taboolib.common.platform.function

import taboolib.common.platform.PlatformFactory
import taboolib.common.platform.service.PlatformIO

val pluginId: String
    get() = PlatformFactory.getPlatformService<PlatformIO>().pluginId

val pluginVersion: String
    get() = PlatformFactory.getPlatformService<PlatformIO>().pluginVersion

val isPrimaryThread: Boolean
    get() = PlatformFactory.getPlatformService<PlatformIO>().isPrimaryThread

fun <T> server() =
    PlatformFactory.getPlatformService<PlatformIO>().server<T>()

fun info(vararg message: Any?) {
    PlatformFactory.getPlatformService<PlatformIO>().info(*message)
}

fun severe(vararg message: Any?) {
    PlatformFactory.getPlatformService<PlatformIO>().severe(*message)
}

fun warning(vararg message: Any?) {
    PlatformFactory.getPlatformService<PlatformIO>().warning(*message)
}

fun releaseResourceFile(path: String, replace: Boolean = false) =
    PlatformFactory.getPlatformService<PlatformIO>().releaseResourceFile(path, replace)

fun getJarFile() =
    PlatformFactory.getPlatformService<PlatformIO>().getJarFile()

fun getDataFolder() =
    PlatformFactory.getPlatformService<PlatformIO>().getDataFolder()

fun getPlatformData() =
    PlatformFactory.getPlatformService<PlatformIO>().getPlatformData()
