package taboolib.common.io

import java.io.File
import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.LinkOption
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future

/**
 * 深度删除专用线程池。
 *
 * 使用非 daemon 线程，避免删除任务尚未完成时 JVM 直接退出导致目录残留；
 * 同时避免占用 [java.util.concurrent.ForkJoinPool.commonPool] 影响其他并行任务。
 */
private val deleteExecutor: ExecutorService = Executors.newSingleThreadExecutor { runnable ->
    Thread(runnable, "TabooLib-FileDelete").apply { isDaemon = false }
}

/**
 * Delete the directory and all its contents asynchronously.<br/>
 * if you need to wait for the deletion to complete, pass in a set here and use Set#forEach(Future<*>::get)
 * @param await whether to wait for the deletion to complete
 * @param futures the future set to store future in
 *
 * @author Kylepoops
 */
fun File.deepDeleteAsync(await: Boolean = false, futures: MutableSet<Future<*>>? = null) {
    // Traverse the whole tree in one asynchronous task. Submitting child tasks and waiting for them
    // from the same bounded executor can exhaust every worker and deadlock on sufficiently deep trees.
    val future = CompletableFuture.runAsync({ deleteTree(toPath()) }, deleteExecutor)
    futures?.add(future)
    if (await) {
        future.get()
    }
}

private fun deleteTree(root: Path) {
    if (!Files.exists(root, LinkOption.NOFOLLOW_LINKS)) {
        return
    }
    Files.walkFileTree(root, object : SimpleFileVisitor<Path>() {

        override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
            try {
                Files.deleteIfExists(file)
            } catch (ex: IOException) {
                // 文件被占用（Windows 上常见于热重载期间被 IDE 或服务端持有），跳过而非中止整棵树
            }
            return FileVisitResult.CONTINUE
        }

        override fun visitFileFailed(file: Path, exc: IOException): FileVisitResult {
            // 无法访问的文件不应中断遍历，否则同目录下其余文件将全部残留
            return FileVisitResult.CONTINUE
        }

        override fun postVisitDirectory(dir: Path, exc: IOException?): FileVisitResult {
            try {
                Files.deleteIfExists(dir)
            } catch (ex: IOException) {
                // 目录非空（其中存在无法删除的文件）时保留，不向上传播
            }
            return FileVisitResult.CONTINUE
        }
    })
}
