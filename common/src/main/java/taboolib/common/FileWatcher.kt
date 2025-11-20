package taboolib.common

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.io.File
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.StandardWatchEventKinds.*
import java.nio.file.WatchService
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 文件事件密封类，表示文件系统的变化事件
 *
 * @property file 发生变化的文件
 */
sealed class FileEvent(val file: File) {
    /**
     * 文件创建事件
     */
    class Create(file: File) : FileEvent(file)

    /**
     * 文件修改事件
     */
    class Modify(file: File) : FileEvent(file)

    /**
     * 文件删除事件
     */
    class Delete(file: File) : FileEvent(file)
}

/**
 * 监视指定文件夹的变化，返回文件事件流
 *
 * 该函数会创建一个独立的监视线程，使用 Java NIO WatchService 监听文件夹的创建、修改和删除事件。
 * 返回的 Flow 会持续发出文件事件，直到 Flow 被取消。
 *
 * @param path 要监视的文件夹路径
 * @return 文件事件流，发出 [FileEvent.Create]、[FileEvent.Modify] 或 [FileEvent.Delete] 事件
 */
fun watchFolder(path: Path): Flow<FileEvent> {
    return callbackFlow {
        val watchThread = object : Thread("FileWatcher-${path.fileName}") {

            private val running = AtomicBoolean(true)
            private lateinit var watchService: WatchService

            /**
             * 注销文件监视器，停止监视线程
             */
            fun unregisterWatcher() {
                running.set(false)

                runCatching {
                    watchService.close()
                }
            }

            override fun run() {
                runCatching {
                    val fileSystem = FileSystems.getDefault()
                    watchService = fileSystem.newWatchService()

                    // 注册文件夹的创建、修改、删除事件
                    path.register(watchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE)

                    while (running.get()) {
                        // 每5秒轮询一次事件
                        val key = watchService.poll(5, TimeUnit.SECONDS) ?: continue
                        val watchedPath = key.watchable() as Path

                        // 处理所有待处理的事件
                        for (event in key.pollEvents()) {
                            val file = watchedPath.resolve(event.context() as Path).toFile()
                            when (event.kind()) {
                                ENTRY_CREATE -> channel.trySendBlocking(FileEvent.Create(file))
                                ENTRY_MODIFY -> channel.trySendBlocking(FileEvent.Modify(file))
                                ENTRY_DELETE -> channel.trySendBlocking(FileEvent.Delete(file))
                            }
                        }

                        // 重置 key，以便继续接收事件
                        if (!key.reset()) {
                            // key 无效，可能是目录被删除了
                            break
                        }
                    }
                }.onFailure { throwable ->
                    // 发生异常时关闭 channel
                    channel.close(throwable)
                }
            }
        }

        watchThread.start()

        // 当 Flow 被取消时，清理资源
        awaitClose {
            watchThread.unregisterWatcher()
            if (watchThread.isAlive) watchThread.interrupt()
        }
    }
}

/**
 * 监视指定文件的变化
 *
 * 该函数会监视单个文件的变化事件。如果该文件所在的父文件夹已经在被监视中，
 * 则直接添加回调函数到现有的监视流中；否则创建一个新的 [WatchFlow]。
 *
 * @param filePath 要监视的文件路径
 * @param func 文件事件回调函数，当文件发生变化时调用
 * @return 如果创建了新的监视流，则返回 [WatchFlow]；如果复用了现有监视流，则返回 null
 */
fun watchFile(filePath: Path, func: (event: FileEvent) -> Unit): WatchFlow? {
    val parent = filePath.parent
    // 注册文件的监视回调
    FileWatcher.watchingFiles.getOrPut(parent) { ConcurrentHashMap() }[filePath] = func

    // 如果父文件夹已在监视中，直接复用
    return if (FileWatcher.watchingFolderJob.containsKey(parent)) {
        null
    } else {
        // 创建新的文件夹监视流
        WatchFlow(parent, watchFolder(parent).onEach { event ->
            FileWatcher.watchingFiles[parent]?.get(event.file.toPath())?.invoke(event)
        })
    }
}

/**
 * 停止监视指定文件
 *
 * 移除文件的监视回调，如果该文件所在的父文件夹没有其他文件在被监视，
 * 则同时取消父文件夹的监视任务。
 *
 * @param filePath 要停止监视的文件路径
 */
fun stopWatchingFile(filePath: Path) {
    FileWatcher.watchingFiles[filePath.parent]?.remove(filePath)
    // 如果父文件夹下没有其他文件在被监视，则取消文件夹的监视任务
    if (FileWatcher.watchingFiles[filePath.parent]?.isEmpty() == true) {
        FileWatcher.watchingFolderJob.remove(filePath.parent)?.cancel()
    }
}

/**
 * 文件监视流包装类
 *
 * 包装了文件夹的监视流，提供便捷的启动方法，并管理监视任务的生命周期。
 *
 * @property path 被监视的文件夹路径
 * @property flow 文件事件流
 */
class WatchFlow(val path: Path, val flow: Flow<FileEvent>): Flow<FileEvent> by flow {

    /**
     * 在指定的协程作用域中启动监视流
     *
     * 如果该文件夹已经有正在运行的监视任务，会先取消旧任务，然后启动新任务。
     *
     * @param scope 协程作用域
     * @return 启动的协程 Job
     */
    fun start(scope: CoroutineScope): Job {
        FileWatcher.watchingFolderJob[path]?.cancel()
        val job = flow.launchIn(scope)
        FileWatcher.watchingFolderJob[path] = job
        return job
    }
}

/**
 * 文件监视器管理对象
 *
 * 用于管理所有正在运行的文件监视任务和回调函数。
 * 采用文件夹级别的监视策略，多个文件可以共享同一个文件夹监视流。
 */
object FileWatcher {

    /**
     * 文件夹监视任务映射表
     *
     * Key: 文件夹路径
     * Value: 监视任务的协程 Job
     */
    val watchingFolderJob = ConcurrentHashMap<Path, Job>()

    /**
     * 文件监视回调映射表
     *
     * Key: 父文件夹路径
     * Value: 该文件夹下被监视的文件及其对应的回调函数
     */
    val watchingFiles = ConcurrentHashMap<Path, ConcurrentHashMap<Path, (event: FileEvent) -> Unit>>()
}