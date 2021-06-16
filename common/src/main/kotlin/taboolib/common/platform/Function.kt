package taboolib.common.platform

import taboolib.common.platform.PlatformFactory.platformAdapter
import taboolib.common.platform.PlatformFactory.platformExecutor
import taboolib.common.platform.PlatformFactory.platformIO
import java.util.*

val isPrimaryThread: Boolean
    get() = platformIO.isPrimaryThread

val runningPlatform: Platform
    get() = platformIO.runningPlatform

fun info(vararg message: Any?) = platformIO.info(*message)

fun severe(vararg message: Any?) = platformIO.severe(*message)

fun warning(vararg message: Any?) = platformIO.warning(*message)

fun releaseResourceFile(path: String, replace: Boolean = false) = platformIO.releaseResourceFile(path, replace)

fun getJarFile() = platformIO.getJarFile()

fun execute(async: Boolean = false, delay: Long = 0, period: Long = 0, executor: PlatformExecutor.PlatformTask.() -> Unit): PlatformExecutor.PlatformTask {
    return platformExecutor.execute(async, delay, period, executor)
}

fun console() = platformAdapter.console()

fun onlinePlayers() = platformAdapter.onlinePlayers()

fun adapterPlayer(any: Any) = platformAdapter.adapterPlayer(any)

fun adapterCommandSender(any: Any) = platformAdapter.adapterCommandSender(any)

fun getProxyPlayer(name: String) = onlinePlayers().firstOrNull { it.name == name }

fun getProxyPlayer(uuid: UUID) = onlinePlayers().firstOrNull { it.uniqueId == uuid }

fun <T> registerListener(event: Class<T>, priority: EventPriority = EventPriority.NORMAL, ignoreCancelled: Boolean = true, func: (T) -> Unit) {
    return platformAdapter.registerListener(event, priority, ignoreCancelled, func)
}

fun callEvent(proxyEvent: ProxyEvent) = platformAdapter.callEvent(proxyEvent)