package taboolib.common.platform

import taboolib.common.platform.PlatformFactory.platformExecutor
import taboolib.common.platform.PlatformFactory.platformIO

val platform: Platform
    get() = platformIO.platform

fun info(vararg message: Any?) = platformIO.info(*message)

fun severe(vararg message: Any?) = platformIO.severe(*message)

fun warning(vararg message: Any?) = platformIO.warning(*message)

fun releaseResourceFile(path: String, replace: Boolean = false) = platformIO.releaseResourceFile(path, replace)

fun getJarFile() = platformIO.getJarFile()

fun execute(async: Boolean = false, delay: Long = 0, period: Long = 0, executor: PlatformExecutor.PlatformTask.() -> Unit): PlatformExecutor.PlatformTask {
    return platformExecutor.execute(async, delay, period, executor)
}