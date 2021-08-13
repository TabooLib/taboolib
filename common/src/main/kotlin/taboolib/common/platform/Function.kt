package taboolib.common.platform

import taboolib.common.OpenContainer
import taboolib.common.TabooLibCommon
import java.io.File
import java.util.*

val pluginId: String
    get() = PlatformFactory.getService<PlatformIO>().pluginId

val pluginVersion: String
    get() = PlatformFactory.getService<PlatformIO>().pluginVersion

val isPrimaryThread: Boolean
    get() = PlatformFactory.getService<PlatformIO>().isPrimaryThread

val runningPlatform: Platform
    get() = TabooLibCommon.getRunningPlatform()

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

fun getOpenContainers(): List<OpenContainer> {
    return PlatformFactory.getService<PlatformIO>().getOpenContainers()
}

fun getOpenContainer(name: String): OpenContainer? {
    return PlatformFactory.getService<PlatformIO>().getOpenContainers().firstOrNull { it.name == name }
}

fun submit(
    now: Boolean = false,
    async: Boolean = false,
    delay: Long = 0,
    period: Long = 0,
    commit: String? = null,
    executor: PlatformExecutor.PlatformTask.() -> Unit,
): PlatformExecutor.PlatformTask {
    return PlatformFactory.getService<PlatformExecutor>().submit(PlatformExecutor.PlatformRunnable(now, async, delay, period, commit, executor))
}

/**
 * 释放在预备阶段的调度器计划
 * 这个方法只能执行一次且必须执行
 */
fun startExecutor() {
    PlatformFactory.getService<PlatformExecutor>().start()
}

fun <T> server(): T {
    return PlatformFactory.getService<PlatformAdapter>().server()
}

fun console(): ProxyCommandSender {
    return PlatformFactory.getService<PlatformAdapter>().console()
}

fun onlinePlayers(): List<ProxyPlayer> {
    return PlatformFactory.getService<PlatformAdapter>().onlinePlayers()
}

fun adaptPlayer(any: Any): ProxyPlayer {
    return PlatformFactory.getService<PlatformAdapter>().adaptPlayer(any)
}

fun adaptCommandSender(any: Any): ProxyCommandSender {
    return PlatformFactory.getService<PlatformAdapter>().adaptCommandSender(any)
}

fun getProxyPlayer(name: String): ProxyPlayer? {
    return onlinePlayers().firstOrNull { it.name == name }
}

fun getProxyPlayer(uuid: UUID): ProxyPlayer? {
    return onlinePlayers().firstOrNull { it.uniqueId == uuid }
}

fun <T> registerListener(event: Class<T>, priority: EventPriority = EventPriority.NORMAL, ignoreCancelled: Boolean = true, func: (T) -> Unit): ProxyListener {
    return PlatformFactory.getService<PlatformAdapter>().registerListener(event, priority, ignoreCancelled, func)
}

fun <T> registerListener(event: Class<T>, level: Int = 0, ignoreCancelled: Boolean = false, func: (T) -> Unit): ProxyListener {
    return PlatformFactory.getService<PlatformAdapter>().registerListener(event, level, ignoreCancelled, func)
}

fun <T> registerListener(event: Class<T>, postOrder: PostOrder = PostOrder.NORMAL, func: (T) -> Unit): ProxyListener {
    return PlatformFactory.getService<PlatformAdapter>().registerListener(event, postOrder, func)
}

fun <T> registerListener(event: Class<T>, order: EventOrder = EventOrder.DEFAULT, beforeModifications: Boolean = false, func: (T) -> Unit): ProxyListener {
    return PlatformFactory.getService<PlatformAdapter>().registerListener(event, order, beforeModifications, func)
}

fun unregisterListener(proxyListener: ProxyListener) {
    PlatformFactory.getService<PlatformAdapter>().unregisterListener(proxyListener)
}

fun callEvent(proxyEvent: ProxyEvent) {
    PlatformFactory.getService<PlatformAdapter>().callEvent(proxyEvent)
}

fun registerCommand(command: CommandStructure, executor: CommandExecutor, completer: CommandCompleter, commandBuilder: CommandBuilder.CommandBase.() -> Unit) {
    PlatformFactory.getService<PlatformCommand>().registerCommand(command, executor, completer, commandBuilder)
}

fun unregisterCommand(command: CommandStructure) {
    unregisterCommand(command.name)
    command.aliases.forEach { unregisterCommand(it) }
}

fun unregisterCommand(command: String) {
    PlatformFactory.getService<PlatformCommand>().unregisterCommand(command)
}

fun unregisterCommands() {
    PlatformFactory.getService<PlatformCommand>().unregisterCommands()
}

inline fun <reified T> implementations(): T {
    return PlatformFactory.getAPI()
}

fun disablePlugin() {
    TabooLibCommon.setStopped(true)
}