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
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

sealed class FileEvent(val file: File) {
    class Create(file: File) : FileEvent(file)
    class Modify(file: File) : FileEvent(file)
    class Delete(file: File) : FileEvent(file)
}

fun watchFolder(path: Path): Flow<FileEvent> {
    return callbackFlow {
        val watchThread = object : Thread() {

            private val running = AtomicBoolean(true)
            private lateinit var watchService: WatchService

            fun unregisterWatcher() {
                running.set(false)

                runCatching {
                    watchService.close()
                }
            }

            override fun run() {
                val fileSystem = FileSystems.getDefault()
                watchService = fileSystem.newWatchService()

                path.register(watchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE)

                while (running.get()) {
                    val key = watchService.poll(5, TimeUnit.SECONDS) ?: continue
                    val path = key.watchable() as Path

                    for (event in key.pollEvents()) {
                        val file = path.resolve(event.context() as Path).toFile()
                        when (event.kind()) {
                            ENTRY_CREATE -> channel.trySendBlocking(FileEvent.Create(file))
                            ENTRY_MODIFY -> channel.trySendBlocking(FileEvent.Modify(file))
                            ENTRY_DELETE -> channel.trySendBlocking(FileEvent.Delete(file))
                        }
                    }

                    key.reset()
                }
            }
        }

        watchThread.start()

        awaitClose {
            watchThread.unregisterWatcher()
            if (watchThread.isAlive) watchThread.interrupt()
        }
    }
}

fun watchFile(filePath: Path, func: (event: FileEvent) -> Unit): WatchFlow? {
    val parent = filePath.parent
    return if (FileWatcher.watchingFolderJob.containsKey(parent)) {
        FileWatcher.watchingFiles.getOrPut(parent) { hashMapOf() }[filePath] = func
        null
    } else {
        FileWatcher.watchingFiles.getOrPut(parent) { hashMapOf() }[filePath] = func
        WatchFlow(parent, watchFolder(parent).onEach { event ->
            FileWatcher.watchingFiles[parent]?.get(event.file.toPath())?.invoke(event)
        })
    }
}

fun stopWatching(filePath: Path) {
    FileWatcher.watchingFiles[filePath.parent]?.remove(filePath)
    if (FileWatcher.watchingFiles[filePath.parent]?.isEmpty() == true) {
        FileWatcher.watchingFolderJob.remove(filePath.parent)?.cancel()
    }
}

class WatchFlow(val path: Path, val flow: Flow<FileEvent>): Flow<FileEvent> by flow {

    fun launchIn(scope: CoroutineScope) {
        FileWatcher.watchingFolderJob[path]?.cancel()
        FileWatcher.watchingFolderJob[path] = flow.launchIn(scope)
    }
}

object FileWatcher {

    val watchingFolderJob = mutableMapOf<Path, Job>()

    val watchingFiles = mutableMapOf<Path, HashMap<Path, (event: FileEvent) -> Unit>>()
}