package taboolib.common.platform

import taboolib.common.OpenContainer
import taboolib.common.TabooLibCommon
import taboolib.common.platform.PlatformFactory.platformAdapter
import taboolib.common.platform.PlatformFactory.platformCommand
import taboolib.common.platform.PlatformFactory.platformExecutor
import taboolib.common.platform.PlatformFactory.platformIO
import java.io.File
import java.util.*

val pluginId: String
    get() = platformIO.pluginId

val isPrimaryThread: Boolean
    get() = platformIO.isPrimaryThread

val runningPlatform: Platform
    get() = TabooLibCommon.getRunningPlatform()

fun info(vararg message: Any?) {
    platformIO.info(*message)
}

fun severe(vararg message: Any?) {
    platformIO.severe(*message)
}

fun warning(vararg message: Any?) {
    platformIO.warning(*message)
}

fun releaseResourceFile(path: String, replace: Boolean = false): File {
    return platformIO.releaseResourceFile(path, replace)
}

fun getJarFile(): File {
    return platformIO.getJarFile()
}

fun getDataFolder(): File {
    return platformIO.getDataFolder()
}

fun getPlatformData(): Map<String, Any> {
    return platformIO.getPlatformData()
}

fun getOpenContainers(): List<OpenContainer> {
    return platformIO.getOpenContainers()
}

fun submit(
    now: Boolean = false,
    async: Boolean = false,
    delay: Long = 0,
    period: Long = 0,
    executor: PlatformExecutor.PlatformTask.() -> Unit,
): PlatformExecutor.PlatformTask {
    return platformExecutor.submit(PlatformExecutor.PlatformRunnable(now, async, delay, period, executor))
}

/**
 * 释放在预备阶段的调度器计划
 * 这个方法只能执行一次且必须执行
 */
fun startExecutor() {
    platformExecutor.start()
}

fun <T> server(): T {
    return platformAdapter.server()
}

fun console(): ProxyConsole {
    return platformAdapter.console()
}

fun onlinePlayers(): List<ProxyPlayer> {
    return platformAdapter.onlinePlayers()
}

fun adaptPlayer(any: Any): ProxyPlayer {
    return platformAdapter.adaptPlayer(any)
}

fun adaptCommandSender(any: Any): ProxyCommandSender {
    return platformAdapter.adaptCommandSender(any)
}

fun getProxyPlayer(name: String): ProxyPlayer? {
    return onlinePlayers().firstOrNull { it.name == name }
}

fun getProxyPlayer(uuid: UUID): ProxyPlayer? {
    return onlinePlayers().firstOrNull { it.uniqueId == uuid }
}

fun <T> registerListener(event: Class<T>, priority: EventPriority = EventPriority.NORMAL, ignoreCancelled: Boolean = true, func: (T) -> Unit): ProxyListener {
    return platformAdapter.registerListener(event, priority, ignoreCancelled, func)
}

fun <T> registerListener(event: Class<T>, level: Int = 0, ignoreCancelled: Boolean = false, func: (T) -> Unit): ProxyListener {
    return platformAdapter.registerListener(event, level, ignoreCancelled, func)
}

fun <T> registerListener(event: Class<T>, postOrder: PostOrder = PostOrder.NORMAL, func: (T) -> Unit): ProxyListener {
    return platformAdapter.registerListener(event, postOrder, func)
}

fun <T> registerListener(event: Class<T>, order: EventOrder = EventOrder.DEFAULT, beforeModifications: Boolean = false, func: (T) -> Unit): ProxyListener {
    return platformAdapter.registerListener(event, order, beforeModifications, func)
}

fun unregisterListener(proxyListener: ProxyListener) {
    platformAdapter.unregisterListener(proxyListener)
}

fun callEvent(proxyEvent: ProxyEvent) {
    platformAdapter.callEvent(proxyEvent)
}

fun registerCommand(command: CommandStructure, executor: CommandExecutor, completer: CommandCompleter, commandBuilder: Command.BaseCommand.() -> Unit) {
    platformCommand.registerCommand(command, executor, completer, commandBuilder)
}

fun unregisterCommand(command: CommandStructure) {
    unregisterCommand(command.name)
    command.aliases.forEach { unregisterCommand(it) }
}

fun unregisterCommand(command: String) {
    platformCommand.unregisterCommand(command)
}

fun unregisterCommands() {
    platformCommand.unregisterCommands()
}